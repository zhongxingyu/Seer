 package net.jonstef.mileage.jdbi;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import net.jonstef.mileage.api.Fillup;
 import net.jonstef.mileage.api.Vehicle;
 import org.h2.jdbcx.JdbcDataSource;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.skife.jdbi.v2.*;
 import org.skife.jdbi.v2.exceptions.TransactionFailedException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.List;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 /**
  *
  */
 public class FillupDAOTest {
 
 	private Logger logger = LoggerFactory.getLogger(FillupDAOTest.class);
 
 	private DBI dbi;
 
 	@Before
 	public void init() {
 		JdbcDataSource ds = new JdbcDataSource();
 		ds.setURL("jdbc:h2:mem:test;AUTOCOMMIT=OFF");
 		ds.setUser("sa");
 		ds.setPassword("sa");
 		dbi = new DBI(ds);
 		dbi.registerArgumentFactory(new DateTimeArgumentFactory());
 		dbi.registerMapper(new FillupMapper());
 	}
 
 	@After
 	public void destroy() {
 	}
 
 	@Test(expected = TransactionFailedException.class)
 	public void testOnDemandInsert() {
		logger.info("BEGIN testOnDemand");
 		Handle h = dbi.open();
 		dbi.getTransactionHandler().inTransaction(h, new VoidTransactionCallback() {
 			@Override
 			protected void execute(Handle handle, TransactionStatus status) throws Exception {
 				FillupDAO dao = handle.attach(FillupDAO.class);
 				dao.createTable();
 				Fillup fillup = new Fillup(null, 91001, new BigDecimal("12.7"), new DateTime(), Vehicle.ACURA.name());
 				dao.insert(fillup);
 				List<Fillup> fillups = dao.getFillups(Vehicle.ACURA.name(), 10);
 				for (Fillup f : fillups) {
 					logger.info("testOnDemand :: {}", f.toString());
 				}
 				dao.deleteById(1l);
 				assertThat(dao.getFillups(Vehicle.ACURA.name(), 10)).isEmpty();
 				status.setRollbackOnly();
 			}
 		});
 		logger.info("END testOnDemand");
 	}
 
 	@Test(expected = TransactionFailedException.class)
 	public void testFetchOnDemand() {
 		logger.info("BEGIN testFetchOnDemand");
 		Handle h = dbi.open();
 		dbi.getTransactionHandler().inTransaction(h, new VoidTransactionCallback() {
 			@Override
 			protected void execute(Handle handle, TransactionStatus status) throws Exception {
 				FillupDAO dao = handle.attach(FillupDAO.class);
 				dao.createTable();
 				List<Fillup> fillups = ImmutableList.of(
 						new Fillup(null, 100000, new BigDecimal("9.50"),  new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 100200, new BigDecimal("10.15"), new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 100400, new BigDecimal("8.79"),  new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 100600, new BigDecimal("9.47"),  new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 100800, new BigDecimal("9.89"),  new DateTime(), Vehicle.ACURA.name())
 				);
 				for (Fillup fillup : fillups) {
 					dao.insert(fillup);
 				}
 				List<Fillup> fetchedFillups = dao.getFillups(Vehicle.ACURA.name(), 3);
 				assertThat(fetchedFillups).hasSize(3);
 				status.setRollbackOnly();
 				logger.info("END testFetchOnDemand");
 			}
 		});
 	}
 
 	@Test(expected = TransactionFailedException.class)
 	public void testDelete() {
 		logger.info("BEGIN testDelete");
 		Handle h = dbi.open();
 		FillupDAO dao = dbi.onDemand(FillupDAO.class);
 		dbi.getTransactionHandler().inTransaction(h, new VoidTransactionCallback() {
 			@Override
 			protected void execute(Handle handle, TransactionStatus status) throws Exception {
 				FillupDAO dao = handle.attach(FillupDAO.class);
 				dao.createTable();
 				List<Fillup> fillups = ImmutableList.of(
 						new Fillup(null, 101000, new BigDecimal("9.50"), new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 101200, new BigDecimal("10.15"), new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 101400, new BigDecimal("8.79"), new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 101600, new BigDecimal("9.47"), new DateTime(), Vehicle.ACURA.name()),
 						new Fillup(null, 101800, new BigDecimal("9.89"), new DateTime(), Vehicle.ACURA.name())
 				);
 				for (Fillup fillup : fillups) {
 					dao.insert(fillup);
 				}
 				List<Fillup> fetchedFillups = dao.getFillups(Vehicle.ACURA.name(), 3);
 				assertThat(fetchedFillups).hasSize(3);
 				Fillup fillupToDelete = fetchedFillups.iterator().next();
 				dao.deleteById(fillupToDelete.getId());
 				fetchedFillups = dao.getFillups(Vehicle.ACURA.name(), 10);
 				Collection<Long> ids = Collections2.transform(fetchedFillups, new Function<Fillup, Long>() {
 					@Nullable
 					@Override
 					public Long apply(@Nullable Fillup fillup) {
 						return fillup.getId();
 					}
 				});
 				assertThat(fetchedFillups).hasSize(4);
 				assertThat(ids).doesNotContain(fillupToDelete.getId());
 				status.setRollbackOnly();
 			}
 		});
 		logger.info("END testDelete");
 	}
 
 	/**
 	 * Demonstrates JDBI's thin layer on top of JDBC. I plan to use the SQL Object API used above.
 	 */
 	@Ignore
 	public void testInsertTheHardWay() {
 		Fillup fillup = dbi.inTransaction(new TransactionCallback<Fillup>() {
 			@Override
 			public Fillup inTransaction(Handle conn, TransactionStatus status) throws Exception {
 				conn.execute("create table if not exists fillup (\n" +
 						"\tid bigint generated by default as identity,\n" +
 						"\tdate timestamp,\n" +
 						"\tmileage integer not null check (mileage>=1 AND mileage<=500000),\n" +
 						"\tquantity decimal(19,2) not null,\n" +
 						"\tvehicle varchar(16),\n" +
 						"\tprimary key (id),\n" +
 						"\tunique (mileage, vehicle)\n" +
 						")");
 				conn.createStatement("insert into fillup(date, mileage, quantity, vehicle) values (:date, :mileage, :quantity, :vehicle)")
 						.bind("date", new java.util.Date())
 						.bind("mileage", 100123)
 						.bind("quantity", new BigDecimal("13.3"))
 						.bind("vehicle", Vehicle.ACURA.name())
 						.execute();
 				List<Fillup> fillups = conn.createQuery("select id, date, mileage, quantity, vehicle from fillup where vehicle = :vehicle")
 						.bind("vehicle", Vehicle.ACURA.name())
 						.mapTo(Fillup.class)
 						.list(1);
 				return fillups.iterator().next();
 			}
 		});
 		System.out.println(fillup);
 	}
 }
