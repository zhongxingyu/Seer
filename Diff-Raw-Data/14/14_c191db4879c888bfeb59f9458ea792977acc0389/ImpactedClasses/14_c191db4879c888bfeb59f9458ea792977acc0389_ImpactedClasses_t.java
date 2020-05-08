 package safeEvolution.approaches.optimizations;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 import safeEvolution.fileProperties.FilePropertiesObject;
 import soot.Main;
 import br.edu.ufcg.dsc.builders.ProductBuilder;
 import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
 import br.edu.ufcg.dsc.Constants;
 import br.edu.ufcg.dsc.Lines;
 import br.edu.ufcg.dsc.Product;
 import br.edu.ufcg.dsc.ProductLine;
 import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
 import br.edu.ufcg.dsc.saferefactor.CommandLine;
 import br.edu.ufcg.dsc.util.AssetNotFoundException;
 import br.edu.ufcg.dsc.util.DirectoryException;
 import br.edu.ufcg.dsc.util.FileManager;
 
 public abstract class ImpactedClasses {
 	
 	private ProductBuilder productBuilder;
 	
 	/** A string collection of changed classes.*/
 	protected Collection<String> modifiedClasses;
 	
 	/** Aspects List*/
 	private HashSet<String> aspectList;
 	
 	/** Hashset of dependencies to the compile the file. */
 	private HashSet<String> dependencies;
 	
 	private HashMap<HashSet<String>, HashSet<String>> aspectsSourceFeatureMapping;
 	
 	private HashMap<HashSet<String>, HashSet<String>> aspectsTargetFeatureMapping;
 	
 	private HashSet<String> constantsPreProcessor;
 	
 	private String uniqueProductPath;
 	private File sourceProductFile;
 	private File targetProductFile;
 	protected FilePropertiesObject input;
 	private String classes;
 	private String classeToGenerateTestes;
 	
 	private HashSet<HashSet<String>> modifiedProducts;
 	
 	private HashSet<HashSet<String>> pseudoProductsToBePreprocessed;
 	
 	private ArrayList<File> filesToTrash;
 	
 	public ImpactedClasses(ProductBuilder productBuilder, FilePropertiesObject in, Collection<String> modifiedClasses) {
 		super();
 		this.productBuilder = productBuilder;
 		this.input = in;
 		this.dependencies = new HashSet<String>();
 		this.aspectList = new HashSet<String>();
 		this.modifiedProducts = new HashSet<HashSet<String>>();
 		this.pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();
 		this.aspectsSourceFeatureMapping = new HashMap<HashSet<String>, HashSet<String>>();
 		this.aspectsTargetFeatureMapping = new HashMap<HashSet<String>, HashSet<String>>();
 		this.filesToTrash = new ArrayList<File>();
 		this.constantsPreProcessor = new HashSet<String>();
 		this.modifiedClasses = modifiedClasses;
 		this.classes = new String();
 		this.classeToGenerateTestes = new String();
 	}
 	
 	private boolean CheckProductBehavior(ProductLine sourceSPL, ProductLine targetSPL, boolean sameBehavior, HashSet<String> prod) throws AssetNotFoundException, DirectoryException, IOException {
 		printPseudoProduct(prod); 
		if(this.input.isAspectsInSourceSPL())
			processModifiedAspect(sourceSPL, this.aspectsSourceFeatureMapping.get(prod), this.sourceProductFile); // Copy modified aspects and its dependencies to the source SPL product folder.	
		if(this.input.isAspectsInTargetSPL())
			processModifiedAspect(targetSPL, this.aspectsTargetFeatureMapping.get(prod), this.targetProductFile); // Copy modified aspects and its dependencies to the target SPL product folder.
		
 		preProcessAssets(sourceSPL, targetSPL, this.input, prod);
 		System.out.println("\nClasses que receberao testes JUNIT: " + classes);
 		sameBehavior = sameBehavior && CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, this.input , false, false);
 		deleteFileToTrash(); // delete files, clean dependencies and aspectsList
 		return sameBehavior;
 	}
 
 	public boolean evaluate(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, boolean wf, boolean areAllProductsMatched) throws AssetNotFoundException, IOException, DirectoryException{
 		boolean isRefinement = false;
 		if(wf && areAllProductsMatched){
 			return checkAssetMappingBehavior(sourceSPL, targetSPL, changedFeatures);	
 		}else{  // Create an Exception!!
 			System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
 		}
 		return isRefinement;
 		
 	}
 
 	protected boolean checkAssetMappingBehavior(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures) throws AssetNotFoundException, DirectoryException, IOException {
 		boolean sameBehavior = true;
 		this.printListofModifiedClasses();
 		long startedTime = System.currentTimeMillis();
 		SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(1); // Set the amout of compiled products to 1
 		createProductFolderStructure(sourceSPL, targetSPL);
 		copyLibrariesFromSplToProductFolder(sourceSPL, targetSPL, this.uniqueProductPath);
 		copyModifiedClassAndDependencies(sourceSPL, targetSPL); // Copy modified class and its dependencies to the SPL product folder.
 		findModifiedProducts(sourceSPL); // Check products that have any of the modified classes. Store it in this.modifiedProducts
 		if (sourceSPL.temAspectos() || (this.input.getLine() == Lines.DEFAULT)){ 
 				findPseudoProductsToPreProcess(sourceSPL, targetSPL); // find which products contains assets with Conditional Compilation - store it in this.pseudoProductsToBePreprocessed
 				for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
 					if (!(sameBehavior = CheckProductBehavior(sourceSPL, targetSPL, sameBehavior, prod))) 
 						break;
 				}
 		}else{
 			this.getPreProcessorConstantes(this.sourceProductFile);
 			if (this.constantsPreProcessor.size() > 0) {
 				findModifiedProducts(sourceSPL, changedFeatures);   // Check products that have any of the modified classes. Store it in this.modifiedProducts
 				findPseudoProductsToPreProcess(); // find which products contains assets with Conditional Compilation - store it in this.pseudoProductsToBePreprocessed
 				SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(this.pseudoProductsToBePreprocessed.size());
 				for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
 					if (!(sameBehavior = CheckProductBehavior(this.input, sameBehavior, prod)))
 						break;
 				}
 			} else { // No preProcess, No aspects
 				this.sourceProductFile.renameTo(new File(this.sourceProductFile.getParent() + Constants.FILE_SEPARATOR + "src"));
 				this.targetProductFile.renameTo(new File(this.targetProductFile.getParent() + Constants.FILE_SEPARATOR + "src"));
 				sameBehavior = CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, this.input , false, false);
 			}
 		}
 		System.out.println("Asset mapping checked in: " + String.valueOf(((System.currentTimeMillis()) - startedTime) / 1000) + " segundos. " + "Same Behavior: " + sameBehavior);
 		return sameBehavior;
 	}
 	
 	private void createProductFolderStructure(ProductLine sourceSPL, ProductLine targetSPL){
 		this.uniqueProductPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR; // This creates product0 directory
 		this.sourceProductFile = FileManager.getInstance().createDir( this.uniqueProductPath + "source" + Constants.FILE_SEPARATOR + (input.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src")); 
 		this.targetProductFile = FileManager.getInstance().createDir( this.uniqueProductPath + "target" + Constants.FILE_SEPARATOR + (input.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));
 		FileManager.getInstance().createDir(this.uniqueProductPath + "source" + Constants.FILE_SEPARATOR + "bin"); // This creates bin folder for SOURCE PRODUCT
 		FileManager.getInstance().createDir(this.uniqueProductPath + "target" + Constants.FILE_SEPARATOR + "bin"); // This creates bin folder for TARGET PRODUCT.
 	}
 	
 	private void setClassToGenerateTests() {
 		if (this.input.getLine() == Lines.TARGET){
 			classeToGenerateTestes = FileManager.getInstance().getPathAPartirDoSrc(classeToGenerateTestes).replaceFirst(Pattern.quote("src.java."), "");
 		}
 		if (classes.isEmpty()){
 			classes = classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
 		} else if (!classes.contains(classeToGenerateTestes.replaceAll(".java", ""))) {
 			classes = classes + "|" + classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
 		}
 	}
 
 	private void isThisClazzFileAspect(File destinationFile) {
 		if (destinationFile.getAbsolutePath().endsWith("aj")) { 
 			this.aspectList.add(destinationFile.getAbsolutePath()); 	// As aspects are not preprocessed, it will be necessary to manually move the aspects to the src folder after the preprocessing.
 		}
 	}
 	
 	/**
 	 *  Check which products have any of the modified classes. 
 	 *	Products that have at least one modified asset.   
 	 */
 	 private void findModifiedProducts(ProductLine sourceSPL) {
 		for (Product product : sourceSPL.getProducts()){
 			HashSet<String> preProcessTags = product.getPreProcessTags();
 			HashMap<String, String> mapeamentoAssetNameParaOrigem = product.getMapeamentoAssetNameParaOrigem();
 			HashMap<String, String> mapeamentoAssetNameParaDestino = product.getMapeamentoAssetNameParaDestino();
 			if (product.containsSomeAsset(this.modifiedClasses, sourceSPL.getMappingClassesSistemaDeArquivos())) {
 				this.modifiedProducts.add(product.getFeaturesList());
 			}
 		}
 	 }
 	 
 	 private void findPseudoProductsToPreProcess(ProductLine sourceSPL, ProductLine targetSPL) {
 		 System.out.println("\n Amount of Pseudo Products: " + pseudoProductsToBePreprocessed.size());	
 		 for (HashSet<String> product : modifiedProducts) {
 				HashSet<String> prodToBuild = this.makeCombination(product, sourceSPL, targetSPL);
 				if (prodToBuild.size() > 0) {
 					pseudoProductsToBePreprocessed.add(prodToBuild);
 				}
 		}
 		SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(pseudoProductsToBePreprocessed.size());
 	}
 	 
 	 private void printPseudoProduct(HashSet<String> prod) {
 			System.out.print("Pseudo Product: ");
 			Iterator<String> it = prod.iterator();
 			String s = "";
 			while(it.hasNext()){
 				s = s + "[ " + it.next() + " ]";
 			}
 			System.out.println(s);
 	}
 	 
 	 /**
 	 * Copy modified class and its dependencies to the SPL product folder.
 	 */
     private void processModifiedClass(String clazz, ProductLine SPL, File splFileDirectory) throws AssetNotFoundException, DirectoryException {
 		String modifiedClassDirectory = SPL.getMappingClassesSistemaDeArquivos().get(clazz); // Get the whole path of the modified class in SOURCE spl file system
 		if (!(modifiedClassDirectory.isEmpty())){
 			String destinationDirectory = splFileDirectory.getAbsolutePath() + FileManager.getInstance().getPathAPartirDoSrc(modifiedClassDirectory).replaceFirst("src", "");
 			File destinationFile = new File(destinationDirectory);
 			FileManager.getInstance().createDir(destinationFile.getParent());
 			FileManager.getInstance().copyFile(modifiedClassDirectory, destinationFile.getAbsolutePath());
 			this.copyDependencies(destinationFile, splFileDirectory, SPL.getMappingClassesSistemaDeArquivos(), null, SPL.getDependencias());
 			isThisClazzFileAspect(destinationFile); // if the answer is YES, add this aspect file to the aspectList
 			setClassToGenerateTests();
 		}
 	}
 	 
     /**
 	 * Copy modified aspects and its dependencies to the SPL product folder.
 	 */
 	private void processModifiedAspect(ProductLine SPL, HashSet<String> aspectsSet, File splFileDirectory) throws AssetNotFoundException,DirectoryException {
 		for (String aspecto : aspectsSet) {
 			String destinationPath = splFileDirectory.getAbsolutePath() + aspecto.split("src")[1];
 			if (!this.modifiedClasses.contains(aspecto) && !this.aspectList.contains(destinationPath)) {
 				File fileDestination = new File(destinationPath);
 				FileManager.getInstance().createDir(fileDestination.getParent()); // create aspect directory
 				FileManager.getInstance().copyFile(SPL.getMappingClassesSistemaDeArquivos().get(FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath()); // copy aspect to the target directory
 				filesToTrash.add(fileDestination);
 				filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
 				this.aspectList.add(fileDestination.getAbsolutePath());
 				this.copyDependencies(fileDestination, splFileDirectory, SPL.getMappingClassesSistemaDeArquivos(), filesToTrash, SPL.getDependencias());
 			}
 		}
 	}
 	
 	private void preProcessFilesWithAntenna(HashSet<String> prod)throws AssetNotFoundException, DirectoryException {
 		/*Mobile Media SPL Files are pre-processed with Antenna Tool. It copies code from  srcprecess folder to src folder*/
 		this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.sourceProductFile.getParent());
 		this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.targetProductFile.getParent());
 		/*Antenna does not process aspects. They are copied to the src folder.*/
 		for (String aspect : this.aspectList) {
 			String dir = new File(aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src")).getParentFile().getAbsolutePath();
 			FileManager.getInstance().createDir(dir);
 			FileManager.getInstance().copyFile(aspect, aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src"));
 		}
 	}
 	
 	private void preProcessFilesWithVelocity(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> prod) throws IOException {
 		/*TaRGeT SPL Files are pre-processed with Velocity Tool - This process is done into the own folder, without copies.
 		 * It is necessary to rename folder to src */
 		if (prod.size() > 0) {
 			this.productBuilder.preprocessVelocity(prod, this.sourceProductFile, sourceSPL, this.sourceProductFile.getParent());
 			this.productBuilder.preprocessVelocity(prod, this.targetProductFile, targetSPL, this.targetProductFile.getParent());
 		}
 		/*Rename srcpreprocess folder to src
 		/*testSourceDirectory.renameTo(new File(testSourceDirectory.getParent()+ System.getProperty("file.separator") + "src"));
 		testTargetDirectory.renameTo(new File(testTargetDirectory.getParent()+ System.getProperty("file.separator") + "src"));*/
 	}
 	
 	private boolean CheckProductBehavior(FilePropertiesObject in, boolean sameBehavior, HashSet<String> prod) throws IOException {
 		this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.sourceProductFile.getParent());
 		this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.targetProductFile.getParent());
 		sameBehavior = sameBehavior	&& CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, in , false, false);
 		return sameBehavior;
 	}
 
 	private void copyModifiedClassAndDependencies(ProductLine sourceSPL, ProductLine targetSPL) throws AssetNotFoundException, DirectoryException {
 		for (String clazz : this.modifiedClasses) { 
 			this.classeToGenerateTestes = clazz;
 			this.processModifiedClass(clazz,sourceSPL, this.sourceProductFile);
 			this.processModifiedClass(clazz,targetSPL, this.targetProductFile);
 		}
 	}
 
 	private void findPseudoProductsToPreProcess() {
 		for (HashSet<String> product : this.modifiedProducts) {
 			HashSet<String> prodToBuild = this.getFeaturesEmComum(product, this.constantsPreProcessor);
 			if (prodToBuild.size() > 0) {
 				this.pseudoProductsToBePreprocessed.add(prodToBuild);
 			}
 		}
 	}
 
 	private void findModifiedProducts(ProductLine sourceSPL, HashSet<String> changedFeatures) {
 		this.modifiedProducts = sourceSPL.getSetsOfFeatures();
 		this.productBuilder.filter(this.modifiedProducts, changedFeatures);
 	}
 
 	/**
 	 * Preprocess files with velocity ou Antenna 
 	 */
 	private void preProcessAssets(ProductLine sourceSPL, ProductLine targetSPL, FilePropertiesObject in, HashSet<String> prod) throws IOException, AssetNotFoundException, DirectoryException {
 		if (in.getLine() == Lines.TARGET || in.getLine() == Lines.DEFAULT) {
 			preProcessFilesWithVelocity(sourceSPL, targetSPL, prod);
 		} else {
 			preProcessFilesWithAntenna(prod);
 		}
 	}
 	
 	/**
 	 *  Delete files, clean dependencies and aspectsList
 	 */
 	private void deleteFileToTrash() {
 		for (File file : filesToTrash) {
 			file.delete();
 			this.aspectList.remove(file.getAbsolutePath());
 			this.dependencies.remove(file.getAbsolutePath());
 		}
 		filesToTrash.clear();
 	}
 
 	/**
 	 * This copies all library files for SOURCE PRODUCT and TARGET PRODUCT lib path. 
 	 *  */
 	private void copyLibrariesFromSplToProductFolder(ProductLine sourceLine, ProductLine targetLine, String productPath) throws AssetNotFoundException, DirectoryException {
 		String sourceLibDirectory = sourceLine.getLibPath(); 
 		String targetLibDirectory = targetLine.getLibPath();
 		if (!(sourceLibDirectory.isEmpty()) && !(targetLibDirectory.isEmpty())) {
 			FileManager.getInstance().copyLibs(sourceLibDirectory, productPath + "source" + Constants.FILE_SEPARATOR + "lib");
 			FileManager.getInstance().copyLibs(targetLibDirectory, productPath + "target" + Constants.FILE_SEPARATOR + "lib");
 		}
 	}
 
 	private void printListofModifiedClasses() {
 		Iterator<String> i = this.modifiedClasses.iterator();
 		System.out.println("\nList of Modified Classes: " + this.modifiedClasses.size());
 		while(i.hasNext()){
 			System.out.println(i.next());
 		}
 		System.out.println("\n--------------------------");
 	}
 	
 	private void copyDependencies(File classe, File destinationDirectory, HashMap<String, String> mapping, ArrayList<File> filesToTrash, HashMap<String, Collection<String>> dependenciasCache) throws AssetNotFoundException, DirectoryException {
 
 		String pathDependencia = FileManager.getInstance().getPathAPartirDoSrc( classe.getAbsolutePath().replaceFirst("srcpreprocess", "src"));
 
 		if (!pathDependencia.startsWith("/")) {
 			pathDependencia = "/" + pathDependencia;
 		}
 
 		Collection<String> dependencias = dependenciasCache.get(pathDependencia.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/"));
 
 		if (dependencias == null) {
 			if (classe.getAbsolutePath().endsWith(".java")) {
 				
 				String clazz =  classe.getName().replaceAll(".java", ""); String path =classe.getParent() ;
 				dependencias = Main.v().getDependences(clazz, path);
 
 				//Classes podem ser dependentes de aspectos
 				//Ocorre quando excecoes lancadas em classes sao tratadas apenas em Aspectos.
 				dependencias.addAll(FileManager.getInstance().getDependenciasAspectos(classe));
 			} else {
 				//Dependencias de aspectos serao identificadas pelo import deles.
 				dependencias = FileManager.getInstance().getDependenciasDeAspectosPeloImport(classe);
 			}
 		}
 
 		for (String dependecia : dependencias) {
 			if (!classe.getAbsolutePath().contains(dependecia) || (!dependecia.contains(".") && !classe.getAbsolutePath().contains("." + dependecia))) {
 
 				String path = this.getPathClassMapping(dependecia, mapping);
 
 				if (path != null) {
 					File file = new File(path);
 
 					// destinationFolder inclui as pastas intermediarias ate o arquivo.
 					// destinationDirectory soh vai ate source ou target.
 					String destinationFolder = destinationDirectory.getAbsolutePath() + FileManager.getInstance().getPathAPartirDoSrc(file.getAbsolutePath()).replaceFirst("src", "");
 
 					File fileDestination = new File(destinationFolder);
 
 					if (this.dependencies.add(fileDestination.getAbsolutePath())) {
 						FileManager.getInstance().createDir(fileDestination.getParent());
 
 						if (!fileDestination.exists()) {
 							FileManager.getInstance().copyFile(file.getAbsolutePath(), fileDestination.getAbsolutePath());
 
 							isThisClazzFileAspect(fileDestination);
 
 							if (filesToTrash != null) {
 								filesToTrash.add(fileDestination);
 								filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
 							}
 
 							this.copyDependencies(file, destinationDirectory, mapping, filesToTrash, dependenciasCache);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param product Modified Product
 	 * @param sourceLine Source SPL
 	 * @param targetLine Target SPL
 	 * @return HashSet<String>
 	 */
 	private HashSet<String> makeCombination(HashSet<String> product, ProductLine sourceLine, ProductLine targetLine) {
 		
 		HashSet<String> result = new HashSet<String>();
 
 		//Como o mapeamento nao mudou, os aspectos da versao source e da target sao os mesmos.
 		ArrayList<String> aspectosDoProdutoSource = this.getAspectosDoProduto(product, sourceLine);
 
 		//Os que interferem podem ter mudado j� que o conte�do deles mudou, podento ter pointcuts e advices novos.
 		HashSet<String> aspectosQueInterferemNaClasseModificadaSource = this.getAspectosQueInterferemNaClasseModificada(aspectosDoProdutoSource, sourceLine.getMappingClassesSistemaDeArquivos());
 		HashSet<String> aspectosQueInterferemNaClasseModificadaTarget = this.getAspectosQueInterferemNaClasseModificada(aspectosDoProdutoSource, targetLine.getMappingClassesSistemaDeArquivos());
 
 		for (String feature : product) {
 			HashMap<String, String> FeaturesConstants = this.productBuilder.getPreprocessFeaturesToConstants();
 			String feat = FeaturesConstants.get(feature);
 			System.out.println("\n feat: " + feat);
 			if (feat != null) {
 				result.add(feature);
 			}
 		}
 
 		boolean jaExiste = false;
 
 		//Verifica se o mesmo conjunto de features j� foi mapeado em outro conjunto de aspectos.
 		if (!this.aspectsSourceFeatureMapping.values().contains(aspectosQueInterferemNaClasseModificadaSource)) {
 			if (this.aspectsSourceFeatureMapping.containsKey(result) || result.isEmpty()) {
 				result.add("fake" + Math.random());
 			}
 
 			this.aspectsSourceFeatureMapping.put(result, aspectosQueInterferemNaClasseModificadaSource);
 			this.aspectsTargetFeatureMapping.put(result, aspectosQueInterferemNaClasseModificadaTarget);
 		} else {
 			for (HashSet<String> prodKey : this.aspectsSourceFeatureMapping.keySet()) {
 				if (this.aspectsSourceFeatureMapping.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaSource) && this.aspectsSourceFeatureMapping.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaTarget)) {
 					jaExiste = this.equivalent(prodKey, result);
 				}
 			}
 			if (!jaExiste) {
 				if (this.aspectsSourceFeatureMapping.containsKey(result) || result.isEmpty()) {
 					result.add("fake" + Math.random());
 				}
 				this.aspectsSourceFeatureMapping.put(result, aspectosQueInterferemNaClasseModificadaSource);
 				this.aspectsSourceFeatureMapping.put(result, aspectosQueInterferemNaClasseModificadaTarget);
 			}
 		}
 
 		return result;
 	}
 	
 	private String getPathClassMapping(String dependecia, HashMap<String, String> mapping) {
 		String result = null;
 
 		// A dependencia nem sempre eh um nome de classe com pacote. As chaves do
 		// mapping sempre sao.
 		// Eh necessario checar se dependencia faz parte de alguma key.
 		for (String key : mapping.keySet()) {
 			if ((dependecia.contains(".") && key.endsWith(dependecia + ".java")) || key.endsWith("." + dependecia + ".java")
 					|| (dependecia.contains(".") && key.endsWith(dependecia + ".aj")) || key.endsWith("." + dependecia + ".aj")) {
 				result = mapping.get(key);
 
 				break;
 			}
 		}
 
 		return result;
 	}
 	
 	private HashSet<String> getAspectosQueInterferemNaClasseModificada(ArrayList<String> aspectosDoProduto, HashMap<String, String> mapping) {
 
 		HashSet<String> aspectosQueInterferemNaClasseModificada = new HashSet<String>();
 
 		for (String aspecto : aspectosDoProduto) {
 			//As classes em que um aspecto interferem sao informadas na primeira
 			//linha do arquivo aj.
 
 			String nameWithoutSlash = this.replaceBarrasPorSeparator(aspecto);
 			String aspectName = FileManager.getInstance().getCorrectName(nameWithoutSlash);
 			String path = mapping.get(aspectName);
 
 			Collection<String> classesEmQueOAspectoInterfe = FileManager.getInstance().getDependenciasAspectos(new File(path));
 
 			for (String classeModificada : this.modifiedClasses) {
 
 				for (String classeEmQueAspectoInterfere : classesEmQueOAspectoInterfe) {
 					if (classeModificada.contains(classeEmQueAspectoInterfere)) {
 						aspectosQueInterferemNaClasseModificada.add(aspecto);
 					}
 				}
 			}
 		}
 
 		return aspectosQueInterferemNaClasseModificada;
 	}
 	
 	private String replaceBarrasPorSeparator(String aspecto) {
 		String result = "";
 
 		String[] parts = aspecto.split(Pattern.quote("/"));
 
 		for (String part : parts) {
 			if (result.equals("")) {
 				result = part;
 			} else {
 				result = result + Constants.FILE_SEPARATOR + part;
 			}
 
 		}
 
 		return result;
 	}
 	
 	private HashSet<String> getFeaturesEmComum(HashSet<String> product, HashSet<String> constantsPreProcessor) {
 		HashSet<String> result = new HashSet<String>();
 		for (String constant : constantsPreProcessor) {
 			String feat = this.productBuilder.getPreprocessConstantsToFeatures().get(constant);
 			if (product.contains(feat)) {
 				result.add(feat);
 			}
 		}
 		return result;
 	}
 	
 	private void getPreProcessorConstantes(File file) throws IOException {
 		if (file.isDirectory()) {
 			for (File fileFromList : file.listFiles()) {
 				this.getPreProcessorConstantes(fileFromList);
 			}
 		} else {
 			String linha = "";
 
 			FileReader reader = new FileReader(file);
 
 			BufferedReader in = new BufferedReader(reader);
 
 			while ((linha = in.readLine()) != null) {
 				if (linha.contains("#if") || linha.contains("#elif")) {
 					linha = linha.trim();
 
 					String[] parts = linha.split(" ");
 
 					for (String part : parts) {
 
 						//Se nao eh um ifdef, elif ou simbolo logico (||, &&, etc)
 						if (!part.contains("#") && part.length() > 2) {
 							this.constantsPreProcessor.add(part);
 						}
 					}
 				}
 			}
 
 			in.close();
 			reader.close();
 		}
 	}
 	
 	private ArrayList<String> getAspectosDoProduto(HashSet<String> product, ProductLine sourceLine) {
 		ArrayList<String> aspectosDoProduto = new ArrayList<String>();
 
 		ConfigurationKnowledge sourceCK = sourceLine.getCk();
 		HashMap<String, String> evalCKSource = sourceCK.evalCKDestinos(product);
 
 		for (String constant : evalCKSource.keySet()) {
 			//Considerando que o mapeamento seja se 1:1.
 
 			String asset = sourceLine.getAssetMapping().get(constant.trim());
 
 			if (asset.endsWith("aj")) {
 				aspectosDoProduto.add(asset);
 			}
 		}
 
 		return aspectosDoProduto;
 	}
 	
 	private boolean equivalent(HashSet<String> prodKeyAntiga, HashSet<String> resultNovo) {
 		//A prodKeyAntida pode ter feature fake.
 		boolean result = true;
 
 		for (String feat : prodKeyAntiga) {
 			if (!resultNovo.contains(feat) && !feat.contains("fake")) {
 				result = false;
 			}
 
 			if (!result) {
 				break;
 			}
 		}
 		if (result) {
 			for (String feat : resultNovo) {
 				if (!prodKeyAntiga.contains(feat)) {
 					result = false;
 				}
 
 				if (!result) {
 					break;
 				}
 			}
 		}
 
 		return result;
 	}
 	
 	/*..........................................................  Getters and Setters */
 	public void setProductBuilder(ProductBuilder productBuilder) {
 		this.productBuilder = productBuilder;
 	}
 	public ProductBuilder getProductBuilder() {
 		return productBuilder;
 	}
 	public Collection<String> getModifiedClasses() {
 		return modifiedClasses;
 	}
 	public void setModifiedClasses(Collection<String> modifiedClasses) {
 		this.modifiedClasses = modifiedClasses;
 	}
 	
 }
