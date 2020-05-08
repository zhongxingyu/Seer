 package info.kwarc.sally.injection;
 
 import info.kwarc.sally.PricingService;
 import info.kwarc.sally.core.composition.SallyInteraction;
 import info.kwarc.sally.core.doc.DocumentManager;
 import info.kwarc.sally.core.interaction.CallbackManager;
 import info.kwarc.sally.core.net.IConnectionManager;
 import info.kwarc.sally.core.rdf.RDFStore;
 import info.kwarc.sally.core.theo.CookieProvider;
 import info.kwarc.sally.core.theo.IPositionProvider;
 import info.kwarc.sally.core.theo.ScreenCoordinatesProvider;
 import info.kwarc.sally.html.injection.HTMLDocModule;
 import info.kwarc.sally.networking.CometD;
 import info.kwarc.sally.networking.ConnectionManager;
 import info.kwarc.sally.pivot.PivotingService;
 import info.kwarc.sally.planetary.injection.PlanetaryModule;
 import info.kwarc.sally.projects.injection.ProjectDocModule;
 import info.kwarc.sally.sharejs.IDocManager;
 import info.kwarc.sally.sharejs.ShareJS;
 import info.kwarc.sally.sketch.injection.SketchDocModule;
 import info.kwarc.sally.spreadsheet.injection.SpreadsheetModule;
 import info.kwarc.sally.theofx.injection.TheoFX;
 import info.kwarc.sally.theoweb.injection.WebTheoModule;
 import info.kwarc.sissi.bpm.SubtaskCallbackMap;
 import info.kwarc.sissi.model.document.cad.injection.CADModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.name.Names;
 
 public class Configuration extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		install(new SpreadsheetModule());
 		install(new CADModule());
 		install(new PlanetaryModule());
 		install(new TheoFX());
 		install(new WebTheoModule());
 		install(new SketchDocModule());
 		install(new HTMLDocModule());
 		install(new ProjectDocModule());
 		
 		bind(CookieProvider.class);
 		bind(IPositionProvider.class).to(ScreenCoordinatesProvider.class);
 		bind(IConnectionManager.class).to(ConnectionManager.class);
 		bind(SallyInteraction.class);
 		bind(PricingService.class);
 		bind(DocumentManager.class);
 		bind(RDFStore.class);
 		bind(PivotingService.class);
 		
 		bind(String.class).annotatedWith(Names.named("SallyPackage")).toInstance("Sally.pkg");
 				
 		bind(Integer.class).annotatedWith(Names.named("SallyPort")).toInstance(8181);
 		bind(CometD.class);
 		bind(CallbackManager.class);
 		bind(SubtaskCallbackMap.class);
 		bind(IDocManager.class).to(ShareJS.class);
 		
 		bind(String.class).annotatedWith(Names.named("ShareJSCollection")).toInstance("libreoffice");
		bind(String.class).annotatedWith(Names.named("ShareJSURL")).toInstance("http://127.0.0.1:7007");
 		
 		bind(String.class).annotatedWith(Names.named("PlanetaryURL")).toInstance("http://localhost/planetmmt");
 		bind(String.class).annotatedWith(Names.named("PlanetaryEndPoint")).toInstance("sallyrpc");  
 		bind(String.class).annotatedWith(Names.named("PlanetaryUser")).toInstance("sally"); 
 		bind(String.class).annotatedWith(Names.named("PLanetaryPassword")).toInstance("123"); 
 		
 		bind(String.class).annotatedWith(Names.named("KnowledgePackageURL")).toInstance("http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/Sally/LATEST");
 		bind(String.class).annotatedWith(Names.named("KnowledgeBaseUser")).toInstance("admin");
 		bind(String.class).annotatedWith(Names.named("KnowledgeBasePassword")).toInstance("admin");
 	}
 }
