 package parser;
 
 import interpreter.TokenSet;
 
 import model.Quality;
 import model.World;
 import model.WorldObject;
 
 
 import earley.EarleyParser;
 import earley.TreeNode;
 import static grammar.Symbols.*;
 import grammar.WorldGrammar;
 
 
 public class Parser {
 	private static final String SORRY_QUESTION = "Sorry, I didn't understand your question.";
 	private static final String SORRY = "Sorry, I didn't understand that statement.";
 	private static final String SORRY_PARSE = "Sorry, I couldn't parse that statement.";
 	private World world;
 	private EarleyParser earley;
 	private String[] nameList;
 	private int nameIndex;
 	public static boolean DEBUG = true;
 
 	public Parser(World world){
 		this.world = world;
 		this.earley = new EarleyParser(new WorldGrammar());
 		this.nameList = new String[1];
 	}
 
 	public String parse(TokenSet tokens) {
 		nameIndex = 0;
 
 		String[] keywords = new String[tokens.tokens.size()];
 		for(int i=0; i<keywords.length; i++)
 			keywords[i] = tokens.tokens.get(i).toString();
 		nameList = tokens.names.toArray(nameList);
 
 		if(!earley.parseSentence(keywords))
 			return SORRY_PARSE;
 		TreeNode root = earley.buildTree();
 		root = root.getChild(0).getChild(0);
 
 		try{
 		return parseSentence(root);
 		}
 		catch(RuntimeException e){
 			return "Error: " + e.getMessage();
 		}
 	}
 
 	private String parseSentence(TreeNode root) {
 		if(DEBUG){
 			TreeNode.printByLevel(root);
 			System.out.println("\n");
 		}
 
 		if(root.data.equals(DECLARATION))
 			return parseDeclaration(root);
 
 		if(root.data.equals(QUESTION))
 			return parseQuestion(root);
 
 		return SORRY;
 	}
 
 	private String parseQuestion(TreeNode root) {
 		if(root.numChildren == 2 && root.getChild(0).data.equals(OBJECT) && root.getChild(1).data.equals(QMARK)){
 			String name = nameList[nameIndex];
 			WorldObject object = findObject(root.getChild(0));
 			if(object == null)
 				return name + " does not exist.";
 			return name + " exists.";
 		}
 		if(root.numChildren == 4 && root.getChild(0).data.equals(IS) && root.getChild(1).data.equals(OBJECT) && root.getChild(2).data.equals(PREP_PHRASE)){
 			String name = nameList[nameIndex];
 			WorldObject object = findObject(root.getChild(1));
 			if(object == null)
 				return name + " does not exist.";
 			Quality[] qualities = getQualities(root.getChild(2));
 			for(Quality q : qualities){
 				if(!object.checkQuality(q)){
 					return "No, " + object.getName() + " is not " + q + ".";
 				}
 			}
 			return "Yes, " + object.getName() + " is " + qualityListToString(qualities) + ".";
 		}
 		
 		if(root.numChildren == 4 && root.getChild(0).data.equals(WHAT) && root.getChild(1).data.equals(IS) && root.getChild(2).data.equals(PREP_PHRASE) && root.getChild(3).data.equals(QMARK)){
 			Quality[] qList = getQualities(root.getChild(2));
 			WorldObject[] matches = world.findMatchingObjects(qList);
 			if(matches.length == 0){
 				return "nothing is " + qualityListToString(qList) + ".";
 			}
 			if(matches.length == 1){
 				return matches[0].getName() + " is " + qualityListToString(qList) + ".";
 			}
 			return objectListToString(matches) + " are " + qualityListToString(qList) + ".";
 		}
 		
 		return SORRY_QUESTION;
 	}
 
 	private String objectListToString(WorldObject[] matches) {
 		String list = matches[0].getName();
 		for(int i=1; i<matches.length-1; i++)
 			list += ", " + matches[i].getName();
 		list += " and " + matches[matches.length-1];
 		return list;
 	}
 
 	private WorldObject findObject(TreeNode objectNode) {
 		String name = nextName();
 		return world.findObject(name);
 	}
 
 	private String parseDeclaration(TreeNode root) {
 
 		if(root.numChildren == 2 && root.getChild(0).data.equals(DECLARATION))
 			return parseDeclaration(root.getChild(0));
 
 		if(root.numChildren == 1 && root.getChild(0).data.equals(OBJECT)){
 			WorldObject object = findOrCreateObject(root.getChild(0));
 			return object.getName();
 		}
 		if(root.numChildren == 3 && root.getChild(0).data.equals(OBJECT) && root.getChild(1).data.equals(IS) && root.getChild(2).data.equals(PREP_PHRASE)){
 			WorldObject object = findOrCreateObject(root.getChild(0));
 			Quality[] qualityList = getQualities(root.getChild(2));
 			String out = object.getName() + " is ";
 			
 			for(Quality q: qualityList)
 				object.setQuality(q);
 			
 			String qString = qualityListToString(qualityList);
 			
 			out += qString + ".";
 			return out;
 		}
 		return SORRY;
 	}
 
 	private String qualityListToString(Quality[] qualityList) {
 		String qString = qualityList[0].toString();
 		for(int i=1; i< qualityList.length-1; i++){
 			qString += qualityList[i] + ", ";
 		}
 		if(qualityList.length > 1){
			qString += " and " + qualityList[qualityList.length-1].toString();
 		}
 		return qString;
 	}
 
 	private Quality[] getQualities(TreeNode prepPhraseRoot) {
 		if(prepPhraseRoot.numChildren == 2 && prepPhraseRoot.getChild(0).data.equals(PREPOSITION) && prepPhraseRoot.getChild(1).data.equals(OBJECT)){
 		return new Quality[]{new Quality(getPreposition(prepPhraseRoot.getChild(0)), findOrCreateObject(prepPhraseRoot.getChild(1)))};
 		}
 		else if(prepPhraseRoot.numChildren == 3 && prepPhraseRoot.getChild(0).data.equals(PREP_PHRASE) && prepPhraseRoot.getChild(1).data.equals(AND) && prepPhraseRoot.getChild(2).data.equals(PREP_PHRASE)){
 			Quality[] qList1 = getQualities(prepPhraseRoot.getChild(0));
 			Quality[] qList2 = getQualities(prepPhraseRoot.getChild(2));
 			Quality[] qListCombined = new Quality[qList1.length + qList2.length];
 			for(int i=0; i<qList1.length; i++)
 				qListCombined[i] = qList1[i];
 			for(int i=0; i<qList2.length; i++)
 				qListCombined[qList1.length+i] = qList2[i];
 			return qListCombined;
 		}
 		return new Quality[0];
 	}
 	
 	private String getPreposition(TreeNode prepRoot){
 		return prepRoot.getChild(0).data.toString();
 	}
 
 	private WorldObject findOrCreateObject(TreeNode objectRoot) {
 		String name = nameList[nameIndex];
 		WorldObject object = findObject(objectRoot);
 		if(object != null)
 			return object;
 		object = new WorldObject(name);
 		world.addObject(object);
 		return object;
 	}
 
 	private String nextName() {
 		return nameList[nameIndex++];
 	}
 }
