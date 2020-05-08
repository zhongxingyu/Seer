 package br.ufal.cideei.visitors;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 
 import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
 import br.ufal.cideei.features.IFeatureExtracter;
 
 public class SupplementaryConfigurationVisitor extends ASTVisitor {
 
 	/** The text selection. */
 	private Set<String> configuration;
 	private HashMap<String,Set<ASTNode>> featureLines;
 	private Set<String> featureNames;
 	private IFile file;
 
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
 	public SupplementaryConfigurationVisitor(Set<String> configuration, IFile file) {
 		this.configuration = configuration;
		featureLines = new HashMap<String,Set<ASTNode>>();
		featureNames = new TreeSet<String>();
		this.file = file;
 	}
 	
 	/**
 	 * Gets the nodes. 
 	 *
 	 * @return the nodes
 	 */
 	public HashMap<String,Set<ASTNode>> getFeatureLines(){
		return featureLines;
 	}
 	
 	public Set<String> getFeatureNames(){
		return featureNames;
 	}
 
 	/**
 	 * Populates the {@link #nodes} Set with the ASTNodes. 
 	 * Use {@link #getNodes()} to retrive the nodes after accepting this visitor to an ASTNode
 	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
 	 */
 	public void preVisit(ASTNode node) {
 		super.preVisit(node);
 		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter();
		Set<String> nodeFeatures = extracter.getFeaturesNames(node, file);
		if (nodeFeatures.size() == 0 || !configuration.containsAll(nodeFeatures)) {
 			Iterator<String> features = nodeFeatures.iterator();
 			String f = null;
 			if(nodeFeatures.size() > 1){
 				while(features.hasNext()){
 					f = features.next();
					featureNames.add(f);
 					this.addNode(node, f);
 				}
 			}else{
 				f = features.next();
 				this.addNode(node, f);
 			}
 		}
 	}
 	
 	private void addNode(ASTNode node, String feature){
		Set<ASTNode> nodes = featureLines.get(feature);
 		if(nodes.size() > 0){
 			 nodes.add(node);
 		}else{
 			nodes = new TreeSet<ASTNode>();
 		}
 		nodes.add(node);
		featureLines.put(feature, nodes);
 	}
 }
 
