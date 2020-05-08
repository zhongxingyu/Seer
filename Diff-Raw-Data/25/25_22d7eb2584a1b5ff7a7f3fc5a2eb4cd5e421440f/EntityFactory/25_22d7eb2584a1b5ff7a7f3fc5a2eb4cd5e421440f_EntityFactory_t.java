 /*
  * Class Name:			EntityFactory.java
  * Class Purpose:		Factory used to generate entities form templates
  * Created by:			boris on 2011-11-04
  */
 package name.bobnet.android.rl.core.ents.factory;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import name.bobnet.android.rl.core.ents.Entity;
 import name.bobnet.android.rl.core.util.TreeNode;
 
 public class EntityFactory {
 
 	// singleton
 	private static EntityFactory entityFactory;
 
 	// variables
 	private TreeNode templates;
 
 	private EntityFactory() {
 		// create the templates tree
 		templates = new TreeNode("root");
 	}
 
 	/**
 	 * Get the Entity factory singleton
 	 * 
 	 * @return the EntityFactory singleton
 	 */
 	public static EntityFactory getEntityFactory() {
 		// create a new factory if needed
 		if (entityFactory == null)
 			entityFactory = new EntityFactory();
 
 		// return the factory
 		return entityFactory;
 	}
 
 	/**
 	 * Add a template to the factory
 	 * 
 	 * @param path
 	 *            The location of the template
 	 * @param t
 	 *            the template to add
 	 */
 	public void addTemplate(String[] path, Template t) {
 		// variables
 		TreeNode cNode = templates;
 
 		// get to the right place in the tree to add the template
 		// create nodes if needed
 		for (String cClass : path)
 			if (cClass != null) {
 				// check if the class exists
 				if (cNode.getChild(cClass) == null) {
 					// create it
 					cNode.addChild(new TreeNode(cClass));
 				}
 
 				// set that as the current node
 				cNode = cNode.getChild(cClass);
 			} else
 				// hit a null class stop searching
 				break;
 
 		// add the template to the node
 		cNode.addChild(new TreeNode(t, t.getName()));
 	}
 
 	/**
 	 * Get a specific entity by class and name
 	 * 
 	 * The class subclass and specclass need to be specified for the template to
 	 * be found
 	 * 
 	 * @param path
 	 *            the path of the template
 	 * @param tName
 	 *            the name of the template
 	 * @return the entity to be generated or <code>null</code> if the entity
 	 *         cannot be found
 	 */
 	public Entity getEntity(String[] path, String tName, Random rnd) {
 		// variables
 		TreeNode cNode = templates;
 		TreeNode templateNode;
 
 		// get to the right place in the tree
 		cNode = templates.getChild(path);
 
 		// make sure the path is good
 		if (cNode == null)
 			// bad path
 			return null;
 
 		// generate the entity
 		templateNode = cNode.getChild(tName);
 		if (templateNode != null)
 			return ((Template) templateNode.getValue()).generate(rnd);
 		else
 			return null;
 	}
 
 	/**
 	 * Get a random entity filtered by class
 	 * 
 	 * @param eClass
 	 *            the class to filter by
 	 * @param eSubClass
 	 *            the subclass to filter by
 	 * @param eSpecClass
 	 *            the Specialisation class to filter by
 	 * @return the entity generated or null if the filtering failed
 	 */
 	public Entity getRndEntity(String[] path, Random rnd) {
 		// variables
 		TreeNode cNode = templates;
 		int roll;
 		ArrayList<Template> goodTemplate;
 
 		// get to the right place in the tree
 		cNode = templates.getChild(path);
 		
 		// check path
 		if (cNode == null) {
 			return null;
 		}
 
 		// get a random Entity
 		do {
 			// roll from 1 to 100
 			roll = rnd.nextInt(100) + 1;
 
 			// create array list of Template that can be generated
 			goodTemplate = new ArrayList<Template>();
 
 			// get templates
 			findGoodTemplates(cNode, roll, goodTemplate);
 
		} while (goodTemplate.isEmpty());
 
 		// pick a random template out of the good ones
 		roll = rnd.nextInt(goodTemplate.size());
 
 		// return a generated entity
 		return goodTemplate.get(roll).generate(rnd);
 
 	}
 
 	private void findGoodTemplates(TreeNode cNode, int roll,
 			ArrayList<Template> res) {
 		// add the good ones to the arraylist
 		for (TreeNode node : cNode.getChildren()) {
 			// check if the node is a template or a class
 			if (node.getValue() == null) {
 				// check all its children
 				findGoodTemplates(node, roll, res);
 			} else if (((Template) node.getValue()).checkRoll(roll)) {
 				// add it to the arraylist
 				res.add((Template) node.getValue());
 			}
 		}
 
 	}
 }
