 /**
  * 
  */
 package org.cotrix.web.share.server.util;
 
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.dsl.Users.*;
 
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.cotrix.common.Outcome;
 import org.cotrix.common.cdi.ApplicationEvents;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.common.Attribute;
 import org.cotrix.domain.dsl.Codes;
 import org.cotrix.domain.dsl.Roles;
 import org.cotrix.domain.dsl.Users;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.io.MapService;
 import org.cotrix.io.ParseService;
 import org.cotrix.io.sdmx.map.Sdmx2CodelistDirectives;
 import org.cotrix.io.sdmx.parse.Stream2SdmxDirectives;
 import org.cotrix.io.tabular.csv.parse.Csv2TableDirectives;
 import org.cotrix.io.tabular.map.ColumnDirectives;
 import org.cotrix.io.tabular.map.Table2CodelistDirectives;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.UserRepository;
 import org.cotrix.security.SignupService;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.Table;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class CodelistLoader {
 
 	protected static final CodeListInfo[] CSV_CODELISTS = new CodeListInfo[]{
 		codelist("ASFIS_MINI.csv", "ASFIS", "3A_CODE", "2011"),
 		codelist("ASFIS_MINI.csv", "ASFIS", "3A_CODE", "2012"),
 		codelist("countries.csv", "COUNTRIES", "ISO 3166-1-alpha-2 code", "1.0")
 	};
 	
 	protected static final CodeListInfo[] SDMX_CODELISTS = new CodeListInfo[]{
 		codelist("CL_SPECIES.xml", "SPECIES", "", ""),
 		codelist("FAO_AREA.xml", "SPECIES", "", ""),
 
 	};
 
 	protected Logger logger = LoggerFactory.getLogger(CodelistLoader.class);
 
 	@Inject
 	protected CodelistRepository repository;
 
 	@Inject
 	protected LifecycleService lifecycleService;
 
 	@Inject
 	protected ParseService service;
 
 	@Inject
 	protected MapService mapservice;
 	
 	@Inject
 	protected SignupService signupService;
 	
 	protected List<User> owners = new ArrayList<User>();
 	
 	@Inject
 	protected UserRepository userRepository;
 	
 	protected void loadUsers() {
 		
 		User user = user().name("federico").email("federico.defaveri@fao.org").fullName("Federico De Faveri").is(Roles.ROOT).build();
 		signupService.signup(user, "federico");
 		owners.add(user);
 		
		user = user().name("fabio").email("fabio.simeoni@fao.org").fullName("Fabio Simeoni").is(Roles.ROOT).build();
 		signupService.signup(user, "fabio");
 		owners.add(user);
 		
 		user = user().name("anton").email("anton.ellenbroek@invented.com").fullName("Anton Ellenbroek").is(Roles.USER).build();
 		signupService.signup(user, "anton");
 		owners.add(user);
 		
 		user = user().name("aureliano").email("gentile.aureliano@invented.com").fullName("Aureliano Gentile").is(Roles.USER, Roles.EDITOR).build();
 		signupService.signup(user, "aureliano");
 		
 		user = user().name("albert").email("abert.einsteink@invented.com").fullName("Albert Einstein").is(Roles.USER).build();
 		signupService.signup(user, "albert");
 		
 		user = user().name("fiorellato").email("fabio.fiorellato@invented.com").fullName("Fabio Fiorellato").is(Roles.USER).build();
 		signupService.signup(user, "fiorellato");
 		
 		user = user().name("baldassarre").email("claudio.baldassarre@invented.com").fullName("Claudio Baldassarre").is(Roles.USER).build();
 		signupService.signup(user, "baldassarre");
 		
 	}
 
 
 	public void importAllCodelist(@Observes ApplicationEvents.Startup event)
 	{
 		loadUsers();
 		for (CodeListInfo codelist:CSV_CODELISTS) {
 			logger.trace("importing "+codelist);
 			boolean imported = importCSVCodelist(codelist);
 			logger.trace("import "+(imported?"complete":"failed"));
 		}
 		
 		for (CodeListInfo codelist:SDMX_CODELISTS) {
 			logger.trace("importing "+codelist);
 			boolean imported = importSDMXCodelist(codelist);
 			logger.trace("import "+(imported?"complete":"failed"));
 		}
 
 		importNumbered();
 		//importSparse();
 		//importComplex();
 		//importDemoCodelist();
 	}
 
 	public boolean importCSVCodelist(CodeListInfo codelistInfo)
 	{
 		logger.trace("importCodelist resourceName: {}, codeColumnName: {}", codelistInfo.getResourceName(), codelistInfo.getCodeColumnName());
 		try {
 			InputStream inputStream = CodelistLoader.class.getResourceAsStream(codelistInfo.getResourceName());
 			Csv2TableDirectives parseDirectives = new Csv2TableDirectives();
 			parseDirectives.options().hasHeader(true);
 			parseDirectives.options().setDelimiter('\t');
 			parseDirectives.options().setQuote('"');
 			Charset charset = Charset.forName("UTF-8");
 			parseDirectives.options().setEncoding(charset);
 
 			Table table = service.parse(inputStream, parseDirectives);
 
 			List<ColumnDirectives> directives = new ArrayList<ColumnDirectives>();
 			Column codeColumn = null;
 			for (Column column:table.columns()) {
 				logger.trace("column name: "+column.name().toString());
 				if (column.name().toString().equals(codelistInfo.getCodeColumnName())) codeColumn = column;
 				else directives.add(new ColumnDirectives(column));
 			}
 			if (codeColumn == null) throw new IllegalArgumentException("Column with name "+codelistInfo.getCodeColumnName()+" not found");
 
 			Table2CodelistDirectives mappingDirectives = new Table2CodelistDirectives(codeColumn);
 			mappingDirectives.name(codelistInfo.getCodelistName());
 			mappingDirectives.version(codelistInfo.getVersion());
 
 			for(ColumnDirectives directive:directives) mappingDirectives.add(directive);
 
 			Attribute att1 = attribute().name("filename").value(codelistInfo.getResourceName()).in("English").build();
 			Attribute att2 = attribute().name("format").value("CSV").in("English").build();
 
 			mappingDirectives.attributes(att1, att2);
 
 			Outcome<Codelist> outcome = mapservice.map(table, mappingDirectives);
 			if (outcome.report().isFailure()) {
 				logger.trace("import failed");
 				return false;
 			}
 
 			Codelist codelist = outcome.result();
 
 			repository.add(codelist);
 			lifecycleService.start(codelist.id());
 			assignOwnership(codelist.id());
 
 			return true;
 		} catch(Exception e)
 		{
 			logger.error("Codelist import failed", e);
 			return false;
 		}
 	}
 	
 	protected void assignOwnership(String codelistId) {
 
 		Role role = Roles.OWNER.on(codelistId);
 		for (User user:owners) {
 			User changeSet = Users.modifyUser(user).is(role).build();
 			userRepository.update(changeSet);
 		}
 	}
 	
 	public boolean importSDMXCodelist(CodeListInfo codelistInfo)
 	{
 		logger.trace("importCodelist resourceName: {}, codeColumnName: {}", codelistInfo.getResourceName(), codelistInfo.getCodeColumnName());
 		try {
 			InputStream inputStream = CodelistLoader.class.getResourceAsStream(codelistInfo.getResourceName());
 
 			CodelistBean bean = service.parse(inputStream, Stream2SdmxDirectives.DEFAULT);
 
 			Outcome<Codelist> outcome = mapservice.map(bean, Sdmx2CodelistDirectives.DEFAULT);
 			if (outcome.report().isFailure()) {
 				logger.trace("import failed");
 				return false;
 			}
 
 			Codelist codelist = outcome.result();
 
 			repository.add(codelist);
 			lifecycleService.start(codelist.id());
 
 			return true;
 		} catch(Exception e)
 		{
 			logger.error("Codelist import failed", e);
 			return false;
 		}
 	}
 	
 	protected void importNumbered()
 	{
 		Codelist codelist = importNumbered(60);
 		repository.add(codelist);
 		lifecycleService.start(codelist.id());
 		assignOwnership(codelist.id());
 	}
 
 	protected Codelist importNumbered(int ncodes)
 	{
 		Code[] codes = new Code[ncodes];
 		for (int i = 1; i <= codes.length; i++) {
 			Attribute[] attributes = new Attribute[]{attribute().name("Row").value(String.valueOf(i)).in("English").build()};
 
 			codes[i-1] = code().name("code "+i).attributes(attributes).build();
 		}
 
 		return Codes.codelist().name("Numbered").with(codes).build();
 	}
 
 	protected void importSparse()
 	{
 		Codelist codelist = createSparseCodelist(60);
 		repository.add(codelist);
 		lifecycleService.start(codelist.id());
 	}
 
 	protected Codelist createSparseCodelist(int ncodes)
 	{
 		Attribute att = attribute().name("format").value("Sparse").in("English").build();
 
 		Code[] codes = new Code[ncodes];
 		for (int i = 0; i < codes.length; i++) {
 			int numAttributes = i/10 + 1;
 			Attribute[] attributes = new Attribute[numAttributes];
 			for (int l = 0; l<attributes.length; l++) attributes[l] = attribute().name("attribute"+l).value("value "+i+"-"+l).in("English").build();
 
 			codes[i] = code().name("code"+i).attributes(attributes).build();
 		}
 
 		return Codes.codelist().name("Sparse").with(codes).attributes(att).build();
 	}
 
 	protected void importComplex()
 	{
 		Codelist codelist = createComplexCodelist(60);
 		repository.add(codelist);
 		lifecycleService.start(codelist.id());
 	}
 
 	protected Codelist createComplexCodelist(int ncodes)
 	{
 		Attribute[] codelistAttributes = new Attribute[3];
 		codelistAttributes[0] = attribute().name("format").value("complex").in("English").build();
 		codelistAttributes[1] = attribute().name("Author").value("Federico").in("English").build();
 		codelistAttributes[2] = attribute().name("Author").value("Fabio").in("English").build();
 
 		String[] languages = new String[]{"En", "Fr", "It"};
 
 		Code[] codes = new Code[ncodes];
 		for (int i = 0; i < codes.length; i++) {
 			int numAttributes = i/10 + 1;
 			List<Attribute> attributes = new ArrayList<Attribute>();
 			for (int l = 0; l<numAttributes; l++) {
 
 				for (String language:languages){
 					Attribute attribute = attribute().name("attLang"+l).value("value "+i+"-"+l+"."+language).in(language).build();
 					attributes.add(attribute);
 				}
 
 				for (int y = 0; y < 3;y++){
 					Attribute attribute = attribute().name("attPos"+l).value("value "+i+"-"+l+".["+y+"]").in("En").build();
 					attributes.add(attribute);
 				}
 			}
 
 			codes[i] = code().name("code"+i).attributes(attributes).build();
 		}
 
 		return Codes.codelist().name("Complex").with(codes).attributes(codelistAttributes).version("2.1").build();
 	}
 	
 	protected void importDemoCodelist()
 	{
 		Codelist codelist = createDemoCodelist();
 		repository.add(codelist);
 		lifecycleService.start(codelist.id());
 	}
 
 
 	protected Codelist createDemoCodelist()
 	{
 		return Codes.codelist().name("Demo Codelist")
 				.with(
 						code().
 						name("4060300201").
 						attributes(
 								attribute().
 								name("description").
 								value("Northern elephant seal").
 								ofType("description").
 								in("en").build(),
 
 								attribute().
 								name("description").
 								value("Éléphant de mer boréal").
 								ofType("description").
 								in("fr").build(),
 								
 								attribute().
 								name("description").
 								value("Foca elephante del norte").
 								ofType("description").
 								in("es").build(),
 								
 								attribute().
 								name("description").
 								value("Mirounga angustirostris").
 								ofType("description").
 								in("la").build(),
 								
 								attribute().
 								name("description").
 								value("Elefante marino settentrionale").
 								ofType("description").
 								in("it").build(),
 								
 								attribute().
 								name("author").
 								value("Gill 1876").
 								ofType("annotation").build()
 								).build(),
 								
 								code().
 								name("4060300202").
 								attributes(
 										attribute().
 										name("description").
 										value("Southern elephant seal").
 										ofType("description").
 										in("en").build(),
 										
 										attribute().
 										name("description").
 										value("Éléphant de mer austral").
 										ofType("description").
 										in("fr").build(),
 										
 										attribute().
 										name("description").
 										value("Foca elephante del sur").
 										ofType("description").
 										in("es").build(),
 										
 										attribute().
 										name("description").
 										value("Mirounga leonina").
 										ofType("description").
 										in("la").build(),
 										
 										attribute().
 										name("description").
 										value("Elefante marino del Sud").
 										ofType("description").
 										in("it").build(),
 										
 										attribute().
 										name("author").
 										value("Linnaeus 1758").
 										ofType("annotation").build()
 										).build(),
 										
 										code().
 										name("4060300203").
 										attributes(
 												attribute().
 												name("description").
 												value("Southern elephant").
 												ofType("description").
 												in("en").build(),
 												
 												attribute().
 												name("description").
 												value("South elephant").
 												ofType("description").
 												in("en").build(),
 												
 												attribute().
 												name("description").
 												value("Éléphant").
 												ofType("description").
 												in("fr").build(),
 												
 												attribute().
 												name("description").
 												value("Éléphant").
 												ofType("annotation").
 												in("fr").build(),
 												
 												attribute().
 												name("author").
 												value("Federico 2013").
 												ofType("annotation").build()	
 												,
 												attribute().
 												name("author").
 												value("Fabio 2013").
 												ofType("annotation").build()
 												).build()).
 
 												attributes(
 														attribute().
 														name("file").
 														value("complex_codelist.txt").
 														ofType("description").in("en").build(),
 														
 														attribute().
 														name("encoding").
 														value("UTF-8").
 														ofType("description").build(),
 														
 														attribute().
 														name("author").
 														value("Federico").
 														ofType("annotation").
 														in("it").build(),
 														
 														attribute().
 														name("author").
 														value("Fabio").
 														ofType("annotation").
 														in("it").build(),
 														
 														attribute().
 														name("author").
 														value("Marco").
 														ofType("description").
 														in("it").build(),
 														
 														attribute().
 														name("author").
 														value("Frederick").
 														ofType("annotation").
 														in("en").build(),
 														
 														attribute().
 														name("author").
 														value("Mark").
 														ofType("description").
 														in("en").build()).
 														version("2.2").build();
 	}
 
 	protected static CodeListInfo codelist(String resourceName, String codelistName, String codeColumnName, String version)
 	{
 
 		return new CodeListInfo(resourceName, codelistName, codeColumnName, version);
 	}
 
 	public static class CodeListInfo {
 		protected String resourceName;
 		protected String codelistName;
 		protected String codeColumnName;
 		protected String version;
 		
 		/**
 		 * @param resourceName
 		 * @param codelistName
 		 * @param codeColumnName
 		 * @param version
 		 */
 		public CodeListInfo(String resourceName, String codelistName,
 				String codeColumnName, String version) {
 			this.resourceName = resourceName;
 			this.codelistName = codelistName;
 			this.codeColumnName = codeColumnName;
 			this.version = version;
 		}
 		/**
 		 * @return the resourceName
 		 */
 		public String getResourceName() {
 			return resourceName;
 		}
 		/**
 		 * @return the codeColumnName
 		 */
 		public String getCodeColumnName() {
 			return codeColumnName;
 		}
 		/**
 		 * @return the version
 		 */
 		public String getVersion() {
 			return version;
 		}
 		
 		/**
 		 * @return the codelistName
 		 */
 		public String getCodelistName() {
 			return codelistName;
 		}
 		/** 
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
 			StringBuilder builder = new StringBuilder();
 			builder.append("CodeListInfo [resourceName=");
 			builder.append(resourceName);
 			builder.append(", codelistName=");
 			builder.append(codelistName);
 			builder.append(", codeColumnName=");
 			builder.append(codeColumnName);
 			builder.append(", version=");
 			builder.append(version);
 			builder.append("]");
 			return builder.toString();
 		}
 	}
 
 }
