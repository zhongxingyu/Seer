 package org.omo.cash;
 
 import java.math.BigDecimal;
 import java.rmi.server.UID;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.apache.activemq.ActiveMQConnection;
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.joda.time.DateMidnight;
 import org.joda.time.Interval;
 import org.joda.time.Period;
 import org.joda.time.ReadableInstant;
 import org.omo.core.DateRange;
 import org.omo.core.Feed;
 import org.omo.core.IntegerRange;
 import org.omo.core.IntervalRoutingStrategy;
 import org.omo.core.IntrospectiveMetadata;
 import org.omo.core.MapModelView;
 import org.omo.core.Metadata;
 import org.omo.core.Model;
 import org.omo.core.ModelView;
 import org.omo.core.Partitioner;
 import org.omo.core.Range;
 import org.omo.core.Registration;
 import org.omo.core.Router;
 import org.omo.core.SimpleModelView;
 import org.omo.core.StringMetadata;
 import org.omo.core.Update;
 import org.omo.core.View;
 import org.omo.core.MapModelView.Adaptor;
 import org.omo.jjms.JJMSConnectionFactory;
 import org.omo.jms.RemotingFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;
 import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
 
 // soak test on big iron and profile...
 
 //  TODO:
 // scalability:
 // work out AMQ incantation to reduce threads - increase dates/accounts/currencies etc
 // can we reduce footprint ? increase trade size and number of trades - how big can we go
 // snapshotting and journalling - consider...
 // how quickly can a snapshot be loaded ?
 // how much impact does making in-vm listeners concurrent have on startup
 // can we deserialise parts of the same snapshot concurrently ?
 // investigate java6 optimisations
 // what can we do to reduce filtering costs
 // if we are only looking at a portion of all the trades (e.g. a 6 week window) does that reduce startup time ?
 // use a sorted container for the queue - sort by  priority
 // could be a doubly linked list with first and last entry of each rank recorded, allowing efficient insertion at tail of any rank...
 // links and messages could be stashed and recycles
 
 // functionality:
 // run partitions from separate spring configs
 // aggregate projections from all partitions
 // produce and aggregate currency projections
 // investigate mono/c# - java comms - xml/protocol-buffers/etc. ?
 // load real currencies - compact
 // load real accounts - sparse
 // load real trades - 150000 ?
 // do 2 currency projections - old (1 month) & new (50 years)
 // aggregate partitioned projections into single models
 // drill down from currency projection, into account projection for that currency
 // an account may be traded in many currencies - change model
 
 public class Server {
 
 	private final DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
 
 	private final int numTrades = 100000;
 	private final int numPartitions = 2;
 	private final int numDays = 5;
 	private final int numAccounts = 10;
 	private final int numCurrencies = 10;
 	private final int numBalances = 100;
 	private final int numCompanies = 10;
 	private final int timeout = 10 * 60 * 1000; // 1 minute
 	private final long feedPeriod = 100L; // millis
 
 	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
 	private final static ExecutorService executorService = Executors.newFixedThreadPool(20);
 	private final static ReadWriteLock lock = new FIFOReadWriteLock(); // needs to be a non-reentrant lock since acquired/released on different threads...
 
 	private final Adaptor<String, String> adaptor = new Adaptor<String, String>() {
 		@Override
 		public String getKey(String value) {
 			return value;
 		}
 	};
 
 	private final Connection internalConnection;
 	private final Session internalSession;
 	private final RemotingFactory<Model<Integer, Trade>> internalTradeRemotingFactory;
 	private final RemotingFactory internalRemotingFactory;
 	private final Connection externalConnection;
 	private final Session externalSession;
 	private final RemotingFactory externalRemotingFactory;
 
 	private final MapModelView<String, String> metaModel;
 	private final Feed<Integer, Trade> tradeFeed;
 
 	public Server(String serverName, ConnectionFactory internalConnectionFactory, ConnectionFactory externalConnectionFactory) throws JMSException, SecurityException, NoSuchMethodException {
 		
 		long start = System.currentTimeMillis();
 		
 		internalConnection = internalConnectionFactory.createConnection();
 		internalConnection.start();
 		internalSession = internalConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 		internalTradeRemotingFactory = new RemotingFactory<Model<Integer, Trade>>(internalSession, Model.class, timeout);
 		internalRemotingFactory = new RemotingFactory(internalSession, Model.class, timeout);
 
 		externalConnection = externalConnectionFactory.createConnection();
 		externalConnection.start();
 		externalSession = externalConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 		externalRemotingFactory = new RemotingFactory(externalSession, Model.class, timeout);
 
 		// build MetaModel
 		{
 			String name = serverName + ".MetaModel";
 			Metadata<String, String> modelMetadata = new StringMetadata("Name");
 			metaModel = new MapModelView<String, String>(name, modelMetadata, adaptor);
 			export(metaModel, true, true);
 		}
 
 		// we'' randomize trade dates out over the next week...
 		// adding TradeFeedr
 		IntrospectiveMetadata<Integer, Trade> tradeMetadata = new IntrospectiveMetadata<Integer, Trade>(Trade.class, "Id");
 		String tradeFeedName = serverName + ".TradeFeed";
 		List<Interval> intervals = createProjection();
 		Date min = intervals.get(0).getStart().toDate();
 		Date max = intervals.get(intervals.size() - 1).getStart().toDate();
 		DateRange dateRange = new DateRange(min, max);
 		{
 			tradeFeed = new Feed<Integer, Trade>(tradeFeedName, tradeMetadata,new IntegerRange(0, numTrades), feedPeriod, new TradeFeedStrategy(dateRange, new IntegerRange(0,numAccounts), new IntegerRange(0, numCurrencies)));
 			export(tradeFeed, false, false);
 		}
 		ProjectionMetaData projectionMetaData = new ProjectionMetaData(intervals);
 		{
 			List<View<Integer, Trade>> partitions = new ArrayList<View<Integer, Trade>>();
 			for (int p = 0; p < numPartitions; p++) {
 
 				String partitionName = serverName + ".Trade.Partition=" + p;
 				ModelView<Integer, Trade> partition = new SimpleModelView<Integer, Trade>(partitionName, tradeMetadata);
 				partitions.add(partition);
 				export(partition, true, false);
 
 				Map<Interval, Collection<View<Integer, Trade>>> dateRangeToViews = new HashMap<Interval, Collection<View<Integer,Trade>>>();
 				for (Interval interval : intervals) {
 					String dayName = partitionName + ".ValueDate="+ dateFormat.format(interval.getStart().toDate());
 					ModelView<Integer, Trade> day = new SimpleModelView<Integer, Trade>(dayName, tradeMetadata);
 					dateRangeToViews.put(interval, Collections.singleton((View<Integer, Trade>) day));
 					export(day, true, false);
 
 					Collection<View<Integer, Trade>> currencyModels = new ArrayList<View<Integer,Trade>>(numCurrencies);
 					for (int c = 0; c < numCurrencies; c++) {
 						String currencyName = dayName + ".Currency=" + c;
 						ModelView<Integer, Trade> currency = new SimpleModelView<Integer, Trade>(currencyName, tradeMetadata);
 
 						Collection<View<Integer, Trade>> accountModels = new ArrayList<View<Integer,Trade>>(numAccounts);
 						for (int a = 0; a < numAccounts; a++) {
 							String accountName = currencyName + ".Account=" + a;
 							ModelView<Integer, Trade> account = new SimpleModelView<Integer, Trade>(accountName, tradeMetadata);
 							accountModels.add(account);
 							export(account, true, false);
 						}
 						Router.Strategy<Integer, Integer, Trade> accountRoutingStrategy = new AccountRoutingStrategy(accountModels);
 						Router<Integer, Integer, Trade> accountRouter = new Router<Integer, Integer, Trade>(accountRoutingStrategy);
 
 						currencyModels.add(currency);
 						export(currency, true, false);
 
 						view(currencyName, accountRouter);
 					}
 					Router.Strategy<Integer, Integer, Trade> currencyRoutingStrategy = new CurrencyRoutingStrategy(currencyModels);
 					Router<Integer, Integer, Trade> currencyRouter = new Router<Integer, Integer, Trade>(currencyRoutingStrategy);
 					view(dayName, currencyRouter);
 
 				}
 				Router.Strategy<Integer, Integer, Trade> intervalRoutingStrategy = new IntervalRoutingStrategy(dateRangeToViews);
 				View<Integer, Trade> dayRouter = new Router<Integer, Integer, Trade>(intervalRoutingStrategy);
 				view(partitionName, dayRouter);
 			}
 			View<Integer, Trade> partitioner = new Partitioner<Integer, Trade>(partitions);
 			view(tradeFeedName, partitioner);
 		}
 
 		//adding AccountFeed
 		{
 			String feedName = serverName + ".AccountFeed";
 			IntrospectiveMetadata<Integer, Account> metadata = new
 			IntrospectiveMetadata<Integer, Account>(Account.class, "Id");
 			Feed.Strategy<Integer, Account> strategy = new Feed.Strategy<Integer, Account>(){
 				@Override
 				public Collection<Account> createNewValues(Range<Integer> range) {
 					Collection<Account> values = new ArrayList<Account>(range.size());
 					for (int id : range.getValues())
 						values.add(new Account(id, 0));
 					return values;
 				}
 
 				@Override
 				public Account createNewVersion(Account original) {
 					return new Account(original.getId(), original.getVersion()+1);
 				}
 
 				@Override
 				public Integer getKey(Account item) {
 					return item.getId();
 				}};
 				Model<Integer, Account> feed = new Feed<Integer, Account>(feedName, metadata, new IntegerRange(0, numAccounts), 100L, strategy);
 				export(feed, true, false);
 		}
 		// adding CurrencyFeed
 		{
 			String feedName = serverName + ".CurrencyFeed";
 			Feed.Strategy<Integer, Currency> strategy = new Feed.Strategy<Integer, Currency>(){
 
 				@Override
 				public Collection<Currency> createNewValues(Range<Integer> range) {
 					Collection<Currency> values = new ArrayList<Currency>(range.size());
 					for (int id : range.getValues())
 						values.add(new Currency(id, 0));
 					return values;
 				}
 
 				@Override
 				public Currency createNewVersion(Currency original) {
 					return new Currency(original.getId(), original.getVersion()+1);
 				}
 
 				@Override
 				public Integer getKey(Currency item) {
 					return item.getId();
 				}};
 				Metadata<Integer, Currency> metadata = new IntrospectiveMetadata<Integer, Currency>(Currency.class, "Id");
 				Model<Integer, Currency> feed = new Feed<Integer, Currency>(feedName, metadata, new IntegerRange(0, numCurrencies), 100L, strategy);
 				export(feed, true, false);
 		}
 		// adding BalanceFeed
 		{
 			String feedName = serverName + ".BalanceFeed";
 			Feed.Strategy<Integer, Balance> strategy = new Feed.Strategy<Integer, Balance>(){
 
 				@Override
 				public Collection<Balance> createNewValues(Range<Integer> range) {
 					Collection<Balance> values = new ArrayList<Balance>(range.size());
 					for (int id : range.getValues())
 						values.add(new Balance(id, 0));
 					return values;
 				}
 
 				@Override
 				public Balance createNewVersion(Balance original) {
 					return new Balance(original.getId(), original.getVersion()+1);
 				}
 
 				@Override
 				public Integer getKey(Balance item) {
 					return item.getId();
 				}};
 				Metadata<Integer, Balance> metadata = new IntrospectiveMetadata<Integer, Balance>(Balance.class, "Id");
 				Model<Integer, Balance> feed = new Feed<Integer, Balance>(feedName, metadata, new IntegerRange(0, numBalances), 100L, strategy);
 				export(feed, true, false);
 		}
 		// adding CompanyFeed
 		{
 			String feedName = serverName + ".CompanyFeed";
 			Feed.Strategy<Integer, Company> strategy = new Feed.Strategy<Integer,
 			Company>(){
 
 				@Override
 				public Collection<Company> createNewValues(Range<Integer> range) {
 					Collection<Company> values = new ArrayList<Company>(range.size());
 					for (int id : range.getValues())
 						values.add(new Company(id, 0));
 					return values;
 				}
 
 				@Override
 				public Company createNewVersion(Company original) {
 					return new Company(original.getId(), original.getVersion()+1);
 				}
 
 				@Override
 				public Integer getKey(Company item) {
 					return item.getId();
 				}};
 				Metadata<Integer, Company> metadata = new IntrospectiveMetadata<Integer, Company>(Company.class, "Id");
 				Model<Integer, Company> feed = new Feed<Integer, Company>(feedName, metadata, new IntegerRange(0, numCompanies), 100L, strategy);
 				export(feed, true, false);
 		}
 
 		// aggregate models
 
 		// Total the trades for each day for a given account within a given
 		// partition
 		for (int p = 0; p < numPartitions; p++) {
 			// build a projection for this account for the following days...
 			String partitionName = serverName + ".Trade.Partition=" + p;
 			// build a projection for this currency for the following days...
 			String currencyProjectionName = partitionName + ".CurrencyProjection";
 			Metadata<Integer, Projection> currencyProjectionMetadata = projectionMetaData;
 			SimpleModelView<Integer, Projection> currencyProjection = new SimpleModelView<Integer, Projection>(currencyProjectionName, currencyProjectionMetadata);
 			export(currencyProjection, true, true);
 
 			for (int c = 0; c < numCurrencies; c++) {
 				String currencyTotalName = partitionName + ".Currency=" + c + ".Total";
 				Metadata<Date, CurrencyTotal> currencyTotalMetadata = new CurrencyTotalMetadata();
 				SimpleModelView<Date, CurrencyTotal> currencyTotal = new SimpleModelView<Date, CurrencyTotal>(currencyTotalName, currencyTotalMetadata);
 				export(currencyTotal, true, false);
 
 				// attach aggregators to Total models to power Projection models
 				String currencyProjectionAggregatorName = "";
 				CurrencyProjectionAggregator currencyProjectionAggregator = new CurrencyProjectionAggregator(currencyProjectionAggregatorName, intervals, c);
 				currencyProjectionAggregator.registerView(currencyProjection);
 				currencyTotal.registerView(currencyProjectionAggregator);
 
 				List<Update<CurrencyTotal>> currencyTotals = new ArrayList<Update<CurrencyTotal>>(intervals.size());
 				for (Interval interval : intervals) {
 					Date d = interval.getStart().toDate();
 					String modelName = partitionName + ".ValueDate=" + dateFormat.format(d) + ".Currency=" + c;
 					String aggregatorName = partitionName + ".ValueDate=" + dateFormat.format(d) + ".Currency=" + c + ".Total";
 					CurrencyAmountAggregator aggregator = new CurrencyAmountAggregator(aggregatorName, d, c);
 					aggregator.registerView(currencyTotal);
 					SimpleModelView<Integer, Trade> model = (SimpleModelView<Integer, Trade>) nameToModel.get(modelName);
 					currencyTotals.add(new Update<CurrencyTotal>(null, new CurrencyTotal(d, 0, c, new BigDecimal(0))));
 					Registration<Integer, Trade> registration = model.registerView(aggregator);
 					Collection<Update<Trade>> insertions = new ArrayList<Update<Trade>>();
 					for (Trade datum : registration.getData())
 						insertions.add(new Update<Trade>(null, datum));
 					aggregator.update(insertions, new ArrayList<Update<Trade>>(), new ArrayList<Update<Trade>>());
 				}
 				currencyTotal.update(currencyTotals, new ArrayList<Update<CurrencyTotal>>(), new ArrayList<Update<CurrencyTotal>>());
 
 				// build account Projections for this currency
 				String accountProjectionName = partitionName + ".Currency=" + c + ".AccountProjection";
 				Metadata<Integer, Projection> accountProjectionMetadata = projectionMetaData;
 				SimpleModelView<Integer, Projection> accountProjection = new SimpleModelView<Integer, Projection>(accountProjectionName, accountProjectionMetadata);
 				export(accountProjection, true, false);
 
 				for (int a = 0; a < numAccounts; a++) {
 					String accountTotalName = partitionName + ".Currency=" + c + ".Account=" + a + ".Total";
 					Metadata<Date, AccountTotal> accountTotalMetadata = new AccountTotalMetadata();
 					SimpleModelView<Date, AccountTotal> accountTotal = new SimpleModelView<Date, AccountTotal>(accountTotalName, accountTotalMetadata);
 					export(accountTotal, true, false);
 
 					// attach aggregators to Total models to power Projection models
 					String accountProjectionAggregatorName = "";
 					AccountProjectionAggregator accountProjectionAggregator = new AccountProjectionAggregator(accountProjectionAggregatorName, intervals, a);
 					accountProjectionAggregator.registerView(accountProjection);
 					accountTotal.registerView(accountProjectionAggregator);
 
 					List<Update<AccountTotal>> accountTotals = new ArrayList<Update<AccountTotal>>(intervals.size());
 					for (Interval interval : intervals) {
 						Date d = interval.getStart().toDate();
 						String modelName = partitionName + ".ValueDate=" + dateFormat.format(d) + ".Currency=" + c + ".Account=" + a;
 						String aggregatorName = partitionName + ".ValueDate=" + dateFormat.format(d) + ".Currency=" + c + ".Account=" + a + ".Total";
 						AccountAmountAggregator aggregator = new AccountAmountAggregator(aggregatorName, d, a);
 						aggregator.registerView(accountTotal);
 						SimpleModelView<Integer, Trade> model = (SimpleModelView<Integer, Trade>) nameToModel.get(modelName);
 						accountTotals.add(new Update<AccountTotal>(null, new AccountTotal(d, 0, a, new BigDecimal(0))));
 						Registration<Integer, Trade> registration = model.registerView(aggregator);
 						Collection<Update<Trade>> insertions = new ArrayList<Update<Trade>>();
 						for (Trade datum : registration.getData())
 							insertions.add(new Update<Trade>(null, datum));
 						aggregator.update(insertions, new ArrayList<Update<Trade>>(), new ArrayList<Update<Trade>>());
 					}
 					accountTotal.update(accountTotals, new ArrayList<Update<AccountTotal>>(), new ArrayList<Update<AccountTotal>>());
 
 				}
 
 			}
 		}
 
 		// Trades for a given Currency for a given Day/Period (aggregated
 		// across all Partitions)
 		for (Interval interval : intervals) {
 			Date d = interval.getStart().toDate();
 			for (int c = 0; c < numCurrencies; c++) {
 				String prefix = serverName + ".Trade";
 				String suffix = ".ValueDate=" + dateFormat.format(d)+ ".Currency=" + c;
 				String name = prefix + suffix;
 				SimpleModelView<Integer, Trade> model = new SimpleModelView<Integer, Trade>(name, tradeMetadata);
 				export(model, true, true);
 				for (int p = 0; p < numPartitions; p++) {
 					view(prefix + ".Partition=" + p + suffix, model);
 				}
 				// Trades for a given Account for a given Day/Period (aggregated across
 				// all Partitions)
 				for (int a = 0; a < numAccounts; a++) {
 					String accountPrefix = serverName + ".Trade";
 					String accountSuffix = ".ValueDate=" + dateFormat.format(d)+ ".Currency=" + c + ".Account=" + a;
 					String accountName = accountPrefix + accountSuffix;
 					SimpleModelView<Integer, Trade> accountModel = new SimpleModelView<Integer, Trade>(accountName, tradeMetadata);
 					export(accountModel, true, false);
 					for (int p = 0; p < numPartitions; p++) {
 						view(accountPrefix + ".Partition=" + p + accountSuffix, accountModel);
 					}
 				}
 			}
 		}
		LOG.info("internal structure completed in {} millis", System.currentTimeMillis() - start);
 	}
 
 	protected final Map<String, Model<?, ?>> nameToModel = new HashMap<String, Model<?, ?>>();
 
 	protected void export(Model model, boolean start, boolean remote) throws JMSException {
 		String name = model.getName();
 		nameToModel.put(name, model);
 		// internal
 		Queue internalQueue = internalSession.createQueue(name);
 		internalRemotingFactory.createServer(model, internalQueue,executorService);
 
 		if (remote) {
 			Queue externalQueue = externalSession.createQueue(name);
 			externalRemotingFactory.createServer(model, externalQueue,executorService);
 			metaModel.update(Collections.singleton(new Update<String>(null, name)), new ArrayList<Update<String>>(), new ArrayList<Update<String>>());
 		}
 		
 		if (start)
 			model.start();
 	}
 
 	protected void view(String modelName, View<Integer, Trade> view) throws IllegalArgumentException, JMSException {
 		Model<Integer, Trade> model;
 		View<Integer, Trade> v;
 		boolean async = false;
 		if (async) {
 			model = internalTradeRemotingFactory.createSynchronousClient(modelName, true);
 			Destination clientDestination = internalSession.createQueue("Client." + new UID().toString()); // tie up this UID with the one in RemotingFactory
 			RemotingFactory<View<Integer, Trade>> serverFactory = new RemotingFactory<View<Integer, Trade>>(internalSession, View.class, timeout);
 			serverFactory.createServer(view, clientDestination, executorService);
 			v = serverFactory.createSynchronousClient(clientDestination, true);
 		} else {
 			model = (Model<Integer, Trade>) nameToModel.get(modelName);
 			v = view;
 		}
 
 		Registration<Integer, Trade> registration = model.registerView(v);
 		Collection<Trade> data = registration.getData();
 		if (data.size() > 0) {
 			Collection<Update<Trade>> insertions = new ArrayList<Update<Trade>>();
 			for (Trade trade : data)
 				insertions.add(new Update<Trade>(null, trade));
 			view.update(insertions, new ArrayList<Update<Trade>>(), new ArrayList<Update<Trade>>());
 		}
 	}
 
 	public void start() throws Exception {
 		long started = System.currentTimeMillis();
 		tradeFeed.start();
 		LOG.info("running in {} seconds",((System.currentTimeMillis() - started) / 1000));
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		long started = System.currentTimeMillis();
 		String name = (args.length == 0 ? "Server" : args[0]);
 		boolean usePeerProtocol = false;
 		String url;
 		// System.setProperty("org.apache.activemq.UseDedicatedTaskRunner", "false"); // causes: RejectedExecutionException - need to inject our own Executor strategy
 		if (usePeerProtocol) {
 			url = "peer://" + name + "/broker0?broker.persistent=false&useJmx=false";
 		} else {
 			url = "vm://" + name +"?marshal=false&broker.persistent=false&create=false";
 			//url = "tcp://localhost:61616";
 			ApplicationContext context = new ClassPathXmlApplicationContext("/application-context.xml");
 			for (String beanName :context.getBeanDefinitionNames())
 				LOG.info("{} : {}", beanName, context.getBean(beanName));
 		}
 		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(url);
 		activeMQConnectionFactory.setOptimizedMessageDispatch(true);
 		activeMQConnectionFactory.setObjectMessageSerializationDefered(true);
 		// activeMQConnectionFactopry.setCopyMessageOnSend(false); // seems to cause NPEs
 		ConnectionFactory externalConnectionFactory = activeMQConnectionFactory;
 		{
 			ActiveMQConnection c = (ActiveMQConnection) externalConnectionFactory.createConnection();
 			LOG.info("org.apache.activemq.UseDedicatedTaskRunner="+ System.getProperty("org.apache.activemq.UseDedicatedTaskRunner"));
 			LOG.info("OptimizedMessageDispatch="+ c.isOptimizedMessageDispatch());
 			LOG.info("ObjectMessageSerializationDeferred="+ c.isObjectMessageSerializationDefered());
 			LOG.info("CopyMessageOnSend=" + c.isCopyMessageOnSend());
 			LOG.info("Broker URL: " + url);
 		}
 		JJMSConnectionFactory internalConnectionFactory = new JJMSConnectionFactory(executorService, new SyncLock(lock.readLock()));
 		internalConnectionFactory.start();
 		Server server = new Server(name, internalConnectionFactory, externalConnectionFactory);
 		LOG.info("started in {} seconds", ((System.currentTimeMillis() - started) / 1000));
 		// keep going...
 		server.start();
 		while (true)
 			try {
 				Thread.sleep(60 * 1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 	}
 
 	
 	protected Interval reverseInterval(ReadableInstant end, ReadableInstant start) {
 		return new Interval(start, end);
 	}
 	
 	public List<Interval> createProjection() {
 		List<Interval> intervals = new ArrayList<Interval>();
 		DateMidnight midnightToday = new DateMidnight();
 		DateMidnight midnight = midnightToday;
 		
 		for (int days = 0; days < 7; days++)
 			intervals.add(reverseInterval(midnight, midnight = midnight.minus(Period.days(1))));
 		for (int weeks = 0; weeks < 2; weeks++)
 			intervals.add(reverseInterval(midnight, midnight = midnight.minus(Period.weeks(1))));
 		for (int months = 0; months < 1; months++)
 			intervals.add(reverseInterval(midnight, midnight = midnight.minus(Period.months(1))));
 		Collections.reverse(intervals);
 		
 		midnight = midnightToday;
 		
 		for (int days = 0; days < 14; days++) 
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.days(1))));
 		for (int weeks = 0; weeks < 2; weeks++)
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.weeks(1))));
 		for (int qtrs = 0; qtrs < 3; qtrs++)
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.months(3))));
 		for (int years = 0; years < 4; years++)
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.years(1))));
 		for (int pentades = 0; pentades < 1; pentades++)
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.years(5))));
 		for (int decades = 0; decades < 4; decades++)
 			intervals.add(new Interval(midnight, midnight = midnight.plus(Period.years(10))));
 
 		return intervals;
 	}
 	
 	// N.B.
 	// 38 intervals * 1339 valid settlement account (857) / currency (201) combinations = 50,882 models
 	// looking in prod copy - extant Flows only seem to excercise 519/1339 of these permutations
 	// 38 * 519 = 19722 - and that assumes that every combination is execrcised for every day - unlikely
 	// I'm doing 5 * 10 * 256 at the moment = 12800
 	// need to do 4 times more
 	// should aggressively build interval/currency models, then add interval/currency/account models lazily
 	// many combinations will not be exercised within trading window loaded, so we should not waste resources building them
 	
 	// all Company/Interval/Currency/Account/Trade Models should be created lazily as trades hit us, to avoid aallocating unnecessary combinations
 }
