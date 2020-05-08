 package cl.votainteligente.inspector.client.inject;
 
 import cl.votainteligente.inspector.client.services.*;
 
 import com.google.gwt.inject.client.AbstractGinModule;
 
 public class ServiceModule extends AbstractGinModule {
 	@Override
 	protected void configure() {
 		bind(BillServiceAsync.class).asEagerSingleton();
 		bind(BillTypeServiceAsync.class).asEagerSingleton();
 		bind(CategoryServiceAsync.class).asEagerSingleton();
 		bind(ChamberServiceAsync.class).asEagerSingleton();
 		bind(CommissionServiceAsync.class).asEagerSingleton();
 		bind(DistrictServiceAsync.class).asEagerSingleton();
 		bind(DistrictTypeServiceAsync.class).asEagerSingleton();
 		bind(InitiativeTypeServiceAsync.class).asEagerSingleton();
 		bind(NotaryServiceAsync.class).asEagerSingleton();
 		bind(ParlamentarianServiceAsync.class).asEagerSingleton();
 		bind(PartyServiceAsync.class).asEagerSingleton();
 		bind(PersonServiceAsync.class).asEagerSingleton();
 		bind(SocietyServiceAsync.class).asEagerSingleton();
 		bind(SocietyTypeServiceAsync.class).asEagerSingleton();
 		bind(StockServiceAsync.class).asEagerSingleton();
 		bind(SubscriberServiceAsync.class).asEagerSingleton();
 		bind(StageServiceAsync.class).asEagerSingleton();
 		bind(UrgencyServiceAsync.class).asEagerSingleton();
 	}
 }
