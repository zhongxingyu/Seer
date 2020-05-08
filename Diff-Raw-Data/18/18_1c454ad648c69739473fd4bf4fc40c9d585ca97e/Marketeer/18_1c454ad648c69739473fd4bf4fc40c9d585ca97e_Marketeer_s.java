 package marketeer;
 
 import java.util.HashMap;
 
 import ontology.MarketOntology;
 import ontology.MarketVocabulary;
 
 import behaviours.BuyBehaviour;
 import behaviours.GUIBehaviour;
 import behaviours.SellBehaviour;
 
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.Property;
 import jade.domain.FIPAAgentManagement.SearchConstraints;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 
 import jade.content.lang.sl.SLCodec;
 import jade.core.Agent;
 
 public class Marketeer extends Agent implements MarketVocabulary {
 	private static final long serialVersionUID = -2478464532305049356L;
 	
 	public static enum PartType {
 		AANSTUURUNIT(2), GLASPLAAT(1), GROTE_PIT(2), KLEINE_PIT(2);
 
 		private int quantity;
 
 		private PartType(int quantity) {
 			this.quantity = quantity;
 		}
 
 		public int getQuantity() {
 			return quantity;
 		}
 	}
 
 	private HashMap<PartType, PartTypeProperties> parts = new HashMap<PartType, PartTypeProperties>();
 
 	private HashMap<PartType, Auction> currentAuctions = new HashMap<PartType, Auction>();
 
 	private int currency = 2000;
 
 	private DFAgentDescription dFagentDescription = new DFAgentDescription();
 
 	@Override
 	public void setup() {
 		Object[] args = getArguments();
 		if(args != null && args.length > 0) {
 			currency = Integer.parseInt((String) args[0]);
 		}
 
 		getContentManager().registerLanguage(new SLCodec());
 		getContentManager().registerOntology(MarketOntology.getInstance());
 
 		try {
 			dFagentDescription.setName(getAID());
 			DFService.register(this, dFagentDescription);
 		} catch(FIPAException e) {
 			e.printStackTrace();
 		}
 
 		parts.put(PartType.AANSTUURUNIT, new PartTypeProperties(2, 30));
 		parts.put(PartType.GLASPLAAT, new PartTypeProperties(1, 50));
 		parts.put(PartType.GROTE_PIT, new PartTypeProperties(2, 20));
 		parts.put(PartType.KLEINE_PIT, new PartTypeProperties(2, 15));
 
 		addBehaviour(new SellBehaviour(this));
 		addBehaviour(new GUIBehaviour(this));
 	}
 
 	@Override
 	public void takeDown() {
 		try {
 			DFService.deregister(this, dFagentDescription);
 		} catch(FIPAException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void sellFurnaceRemains() {
 		int count = Integer.MAX_VALUE;
 		for(PartType partType : PartType.values()) {
 			count = Math.min(count, parts.get(partType).getCount() / partType.getQuantity());
 		}
 		for(PartType partType : PartType.values()) {
 			addCurrentAuction(partType, parts.get(partType).getCount() - count * partType.quantity);
 		}
 	}
 	
 	public DFAgentDescription[] queryForDFAuctions(PartType partType) throws FIPAException {
 		DFAgentDescription query = new DFAgentDescription();
 		query.addOntologies(MarketOntology.NAME);
 		ServiceDescription sd = new ServiceDescription();
 		sd.setName(partType.name());
 		sd.setType("auction");
 		query.addServices(sd);
 
 		SearchConstraints allResults = new SearchConstraints();
 		allResults.setMaxResults(5l);
 		
 		return DFService.search(this, query, allResults);
 	}
 
 	public void addPart(PartType part, int price) {
 		if(parts.containsKey(part)) {
 			PartTypeProperties tp = parts.get(part);
 			tp.addCount(1);
 			parts.put(part, tp);
 		} else {
 			parts.put(part, new PartTypeProperties(1, price));
 		}
 	}
 
 	public void removePart(PartType part) {
 		if(parts.containsKey(part) && parts.get(part).getCount() > 0) {
 			PartTypeProperties properties = parts.get(part);
 			properties.addCount(-1);
 
 			if(part.getQuantity() > properties.getCount()) {
 				addBehaviour(new BuyBehaviour(this, part, properties.getPrice()));
 			}
 		}
 	}
 
 	public boolean hasCurrentAuction(PartType part) {
 		return currentAuctions.containsKey(part);
 	}
 
 	public Auction getCurrentAuction(PartType part) {
 		return currentAuctions.get(part);
 	}
 
 	public void addCurrentAuction(PartType part, int quantity) {
 		if(parts.containsKey(part) && parts.get(part).getCount() > 0 && quantity > 0) {
 			if(currentAuctions.containsKey(part)) {
 				Auction auction = currentAuctions.get(part);
 				auction.addCount(quantity);
 				currentAuctions.put(part, auction);
 			} else {
 				currentAuctions.put(part, new Auction(parts.get(part).getPrice(), quantity));
 				ServiceDescription service = new ServiceDescription();
 				service.setName(part.name());
 				service.setType("auction");
 				service.addLanguages(getContentManager().getLanguageNames()[0]);
 				service.addOntologies("MarketOntology");
 				Property priceProperty = new Property("prijs", currentAuctions.get(part).getCurrentPrice());
 				service.addProperties(priceProperty);
 				dFagentDescription.addServices(service);
 				try {
 					DFService.modify(this, dFagentDescription);
 				} catch(FIPAException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void removeCurrentAuction(PartType part, int quantity) {
 		if(quantity > 0) {
 			Auction auction = currentAuctions.get(part);
 			auction.addCount(-quantity);
 			auction.setCurrentPrice(parts.get(part).getPrice());
 			if(auction.getCount() <= 0) {
 				currentAuctions.remove(part);
 	
 				ServiceDescription service = new ServiceDescription();
 				service.setName(part.name());
 				service.setType("auction");
 				service.addLanguages(getContentManager().getLanguageNames()[0]);
 				service.addOntologies("MarketOntology");
 				Property priceProperty = new Property("prijs", currentAuctions.get(part).getCurrentPrice());
 				service.addProperties(priceProperty);
 				dFagentDescription.removeServices(service);
 				try {
 					DFService.modify(this, dFagentDescription);
 				} catch(FIPAException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void addCurrency(int addCurrency) {
 		currency += addCurrency;
 	}
 
 	public void subCurrency(int subCurrency) {
 		currency -= subCurrency;
 	}
 
 	public int getCurrency() {
 		return currency;
 	}
 }
