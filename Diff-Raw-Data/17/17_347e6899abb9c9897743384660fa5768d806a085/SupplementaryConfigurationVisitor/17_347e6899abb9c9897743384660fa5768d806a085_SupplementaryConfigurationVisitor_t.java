 package br.ufal.cideei.visitors;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 
 import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
 import br.ufal.cideei.features.IFeatureExtracter;
 
 public class SupplementaryConfigurationVisitor extends ASTVisitor {
 
 	/** The text selection. */
	private HashSet<String> configuration;
 	private HashMap<String,Set<ASTNode>> featureLines;
 	private Set<String> featureNames;
 	private IFile file;
 	private IFeatureExtracter extracter;
 
 	/**
 	 * Instantiates a new selection nodes visitor.
 	 */
 	@SuppressWarnings("unused")
 	private SupplementaryConfigurationVisitor() {
 	}
 
 	/**
 	 * Instantiates a new selection nodes visitor.
 	 *
 	 * @param textSelection the text selection
 	 */
	public SupplementaryConfigurationVisitor(HashSet<String> configuration, IFile file) {
 		this.configuration = configuration;
 		this.featureLines = new HashMap<String,Set<ASTNode>>();
 		this.featureNames = new HashSet<String>();
 		this.extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter();
 		this.file = file;
 	}
 	
 	/**
 	 * Gets the nodes. 
 	 *
 	 * @return the nodes
 	 */
 	public HashMap<String,Set<ASTNode>> getFeatureLines(){
 		System.out.println(featureLines);
 		return featureLines;
 	}
 	
 	public Set<String> getFeatureNames(){
 		System.out.println(featureNames);
 		return featureNames;
 	}
 
 	/**
 	 * Populates the {@link #nodes} Set with the ASTNodes. 
 	 * Use {@link #getNodes()} to retrive the nodes after accepting this visitor to an ASTNode
 	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
 	 */
 	public void preVisit(ASTNode node) {
 		super.preVisit(node);
 		Set<String> nodeFeatures = this.extracter.getFeaturesNames(node, this.file);
 		if (!nodeFeatures.isEmpty()){
			if(!this.configuration.containsAll(nodeFeatures)) {
 				Iterator<String> features = nodeFeatures.iterator();
 				String f = null;
 				if(nodeFeatures.size() > 1){
 					while(features.hasNext()){
 						f = features.next();
 						this.addNode(node, f);
 					}
 				}else{
 					f = features.next();
 					this.addNode(node, f);
 				}
 			}
 		}
 	}
 	
 	private void addNode(ASTNode node, String feature){
 		Set<ASTNode> nodes = featureLines.get(feature);
 		if(nodes != null){
 			 nodes.add(node);
 		}else{
 			nodes = new HashSet<ASTNode>();
 			nodes.add(node);
 		}
 		featureNames.add(feature);
 		featureLines.put(feature, nodes);
 	}

 }
 
