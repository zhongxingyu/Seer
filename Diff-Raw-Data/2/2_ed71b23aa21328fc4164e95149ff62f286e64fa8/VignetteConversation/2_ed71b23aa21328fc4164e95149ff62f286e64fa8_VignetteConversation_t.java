 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package es.eucm.eadventure.editor.control.vignette;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.ls.DOMImplementationLS;
 
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 import es.eucm.eadventure.common.data.chapter.conversation.GraphConversation;
 import es.eucm.eadventure.common.data.chapter.conversation.line.ConversationLine;
 import es.eucm.eadventure.common.data.chapter.conversation.node.ConversationNode;
 import es.eucm.eadventure.common.data.chapter.conversation.node.DialogueConversationNode;
 import es.eucm.eadventure.common.data.chapter.conversation.node.OptionConversationNode;
 import es.eucm.eadventure.common.data.chapter.effects.Effects;
 import es.eucm.eadventure.editor.control.writer.domwriters.EffectsDOMWriter;
 
 /**
  *
  * @author mfreire
  */
 public class VignetteConversation {
 	private JsonArray speakers = new JsonArray();
 	private JsonArray nodes = new JsonArray();
 	private JsonArray main = new JsonArray();
 	private JsonObject root = new JsonObject();
 	private int lastSpeakerId = 0;
 	private int lastLineId = 0;
 
 	private static HashMap<String, Effects> effectsCache = new
 			HashMap<String, Effects>();
 
 	public HashMap<ConversationNode, JsonObject> cNodeToLine =
 			new HashMap<ConversationNode, JsonObject>();
 	public HashMap<Integer, JsonObject> idToLine =
 			new HashMap<Integer, JsonObject>();
 	public HashMap<String, Integer> nameToSpeakerId =
 			new HashMap<String, Integer>();
 	public HashMap<Integer, String> speakerIdToName =
 			new HashMap<Integer, String>();
 
 	public VignetteConversation() {
 		root.add("speakers", speakers);
 		root.add("nodes", nodes);
 	}
 
 	public void build(String json) {
 		JsonParser p = new JsonParser();
 		JsonObject input = p.parse(json).getAsJsonObject();
 		speakers.addAll(input.getAsJsonArray("speakers"));
 		for (int i=0; i<speakers.size(); i++) {
 			JsonObject s = speakers.get(i).getAsJsonObject();
 			System.err.println("read speaker: " + s);
 			speakerIdToName.put(s.get("id").getAsInt(),
 				s.getAsJsonPrimitive("name").getAsString());
 		}
 		nodes.addAll(input.getAsJsonArray("nodes"));
 		for (int i=0; i<nodes.size(); i++) {
 			JsonObject n = nodes.get(i).getAsJsonObject();
 			System.err.println("read node: " + n);
 			idToLine.put(n.get("id").getAsInt(), n);
 		}
 	}
 
 	public void build(List<VignetteCharacterPreview> vcps, GraphConversation conv) {
 
 		for (VignetteCharacterPreview vcp : vcps) {
 			addSpeaker(vcp.getName(), "img/m/" + vcp.getImageName());
 		}
 		// create "Effect" speaker
 		addSpeaker("Effect", "img/p/e.png");
 
 		// first pass: create all nodes
 		for (ConversationNode n : conv.getAllNodes()) {
 			registerNode(n);
 		}
 		// second pass: add all links
 		for (ConversationNode n : conv.getAllNodes()) {
 			for (int i=0; i<n.getChildCount(); i++) {
 				addOutgoing(n, n.getChild(i), i);
 			}
 		}
 	}
 
 	private TreeMap<Integer, ConversationNode> idToNode = new TreeMap<Integer, ConversationNode>();
 	private HashMap<Integer, ArrayList<Integer> > outgoing = new HashMap<Integer, ArrayList<Integer> >();
 	private HashMap<Integer, ArrayList<Integer> > incoming = new HashMap<Integer, ArrayList<Integer> >();
 
 	private Effects createEffects(String xml) {
         return effectsCache.get(xml);
 	}
 
 	private void buildDialogueNode(int startId, ConversationNode parent, GraphConversation gc) {
 		if (idToNode.containsKey(startId)) {
 			parent.addChild(idToNode.get(startId));
 			// no need to re-create - already existing
 			return;
 		}
 
 		JsonObject n = idToLine.get(startId);
 
 		int currentId = startId;
 		DialogueConversationNode dNode = (parent != null ?
 				new DialogueConversationNode() :
 				(DialogueConversationNode)gc.getRootNode());
 		if (parent != null) {
 			parent.addChild(dNode);
 		}
 
 		boolean shouldMergeWithNext = true;
 		while (shouldMergeWithNext) {
 			int speakerId = n.getAsJsonPrimitive("speaker").getAsInt();
 			String speaker = speakerIdToName.get(speakerId);
 			if (speaker.equals("Effect")) {
 				String xml = n.getAsJsonPrimitive("text").getAsString();
 				Effects efs = createEffects(xml);
 				if (efs == null) {
 					throw new IllegalArgumentException("Bad effect xml:" + xml);
 				}
 				dNode.setEffects(efs);
 			} else {
 				dNode.addLine(new ConversationLine(
 					speaker,
 					n.getAsJsonPrimitive("text").getAsString()));
 			}
 			idToNode.put(currentId, dNode);
 			System.err.println("Added to dNode " + dNode + ": " + currentId);
 
 			shouldMergeWithNext = false;
 			if (speaker.equals("Effect")) {
 				// end-of-node
 				if (outgoing.get(currentId).size() > 1) {
 					buildOptionNode(currentId, dNode, gc);
 				} else if (outgoing.get(currentId).size() == 1) {
 					buildDialogueNode(outgoing.get(currentId).get(0), dNode, gc);
 				}
 				// should NOT merge
 			} else if (outgoing.get(currentId).size() == 1
 					&& incoming.get(outgoing.get(currentId).get(0)).size() == 1) {
 				currentId = outgoing.get(currentId).get(0);
 				n = idToLine.get(currentId);
 				shouldMergeWithNext = true;
 			} else if (outgoing.get(currentId).size() > 1) {
 				buildOptionNode(currentId, dNode, gc);
 			}
 		}
 	}
 
 	private void buildOptionNode(int parentId, ConversationNode parent, GraphConversation gc) {
 		OptionConversationNode oNode = new OptionConversationNode();
		if (parent != null && parent.getType() != ConversationNode.OPTION) {
 			parent.addChild(oNode);
 		}
 		for (int childId : outgoing.get(parentId)) {
 			if (idToNode.containsKey(childId)) {
 				throw new IllegalArgumentException("ONode child " + childId + " already present");
 			}
 			idToNode.put(childId, oNode);
 			System.err.println("Added to oNode " + oNode + ": " + childId);
 
 			JsonObject n = idToLine.get(childId);
 			String speaker = n.getAsJsonPrimitive("speaker").getAsString();
 			oNode.addLine(new ConversationLine(
 				speaker,
 				n.getAsJsonPrimitive("text").getAsString()));
 			idToNode.put(childId, oNode);
 			if (incoming.get(childId).size() != 1) {
 				throw new IllegalArgumentException("ONode child " + childId + " != 1 input");
 			}
 			if (outgoing.get(childId).size() > 1) {
 				throw new IllegalArgumentException("ONode child " + childId + " != 1 output");
 			} else if (outgoing.get(childId).size() == 0) {
 				int nextId = idToNode.lastKey()+1;
 				DialogueConversationNode dcn = new DialogueConversationNode();
 				oNode.addChild(dcn);
 			} else {
 				int grandChildId = outgoing.get(childId).get(0);
 				buildDialogueNode(grandChildId, oNode, gc);
 				oNode.addChild(idToNode.get(grandChildId));
 			}
 		}
 	}
 
 	public GraphConversation toConversation(String name, String vignetteId) {
 		GraphConversation gc = new GraphConversation(name);
 		gc.setVignetteId(vignetteId);
 		// init in & out
 		for (int i=0; i<nodes.size(); i++) {
 			int src=  nodes.get(i).getAsJsonObject().get("id").getAsInt();
 			outgoing.put(src, new ArrayList<Integer>());
 			incoming.put(src, new ArrayList<Integer>());
 		}
 		// register all links
 		int rootId = -1;
 		for (int i=0; i<nodes.size(); i++) {
 			int src =  nodes.get(i).getAsJsonObject().get("id").getAsInt();
 			JsonObject n = nodes.get(i).getAsJsonObject();
 			if (n.has("start") && n.getAsJsonPrimitive("start").getAsBoolean()) {
 				rootId = n.get("id").getAsInt();
 			}
 			Iterator<JsonElement> it = n.get("outgoing").getAsJsonArray().iterator();
 			while (it.hasNext()) {
 				int dst = it.next().getAsInt();
 				outgoing.get(src).add(dst);
 				incoming.get(dst).add(src);
 			}
 		}
 		// build DialogueNodes and ConversationNodes
 		buildDialogueNode(rootId, null, gc);
 
 		return gc;
 	}
 
 	public final int addSpeaker(String name, String img) {
 		JsonObject speaker = new JsonObject();
 		int assignedId = lastSpeakerId++;
 		speaker.add("id", new JsonPrimitive("" + assignedId));
 		speaker.add("name", new JsonPrimitive(name));
 		if (img != null) {
 			speaker.addProperty("img", img);
 		} else {
 			speaker.addProperty("img", "img/p/c.png");
 		}
 		nameToSpeakerId.put(name, assignedId);
 		speakers.add(speaker);
 		return assignedId;
 	}
 
 	public JsonObject createLine(String speaker, String text, int prev) {
 		JsonObject line = new JsonObject();
 		int assignedId = lastLineId++;
 		if (assignedId == 0) {
 			line.addProperty("start", true);
 		}
 		if ( ! nameToSpeakerId.containsKey(speaker)) {
 			System.err.println("no image for " + speaker);
 			addSpeaker(speaker, null);
 		}
 		line.addProperty("speaker", nameToSpeakerId.get(speaker));
 		line.addProperty("id", assignedId);
 		line.addProperty("text", text);
 		line.add("outgoing", new JsonArray());
 
 		if (prev >= 0) {
 			idToLine.get(prev).getAsJsonArray("outgoing").add(
 					new JsonPrimitive(assignedId));
 		}
 
 		idToLine.put(assignedId, line);
 		nodes.add(line);
 		return line;
 	}
 
 	public JsonObject createEffectLine(Effects efs, int prev) {
 		StringBuilder sb = new StringBuilder();
 		Node node = EffectsDOMWriter.buildDOM("effects", efs);
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance( );
 			DocumentBuilder db = dbf.newDocumentBuilder( );
 			Document doc = db.newDocument( );
 			DOMImplementationLS di = (DOMImplementationLS)doc.getImplementation();
 			String xml = di.createLSSerializer().writeToString(node);
 			// omit <?xml version ... ?>
 			sb.append(xml.substring(xml.indexOf("?>") + "?>".length()));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		effectsCache.put(sb.toString(), efs);
 		return createLine("Effect", sb.toString(), prev);
 	}
 
 	public void registerNode(ConversationNode node) {
 		boolean saved = false;
 		int prev = -1;
 		JsonObject current;
 		for (int i=0; i<node.getLineCount(); i++) {
 			current = createLine(
 					node.getLineName(i), node.getLineText(i),
 						(node.getType() != ConversationNode.OPTION) ? prev : -1);
 			prev = current.get("id").getAsInt();
 			if ( ! saved) {
 				cNodeToLine.put(node, current);
 				saved = true;
 			}
 		}
 		if ( ! node.getEffects().isEmpty()) {
 			current = createEffectLine(node.getEffects(), prev);
 			if ( ! saved) {
 				cNodeToLine.put(node, current);
 				saved = true;
 			}
 		}
 		if ( ! saved) {
 			cNodeToLine.put(node, createLine("Player", "...", -1));
 			saved = true;
 		}
 	}
 
 	public void addOutgoing(ConversationNode from, ConversationNode to, int i) {
 		JsonArray out;
 		int sourceId = cNodeToLine.get(from).get("id").getAsInt();
 		int targetId = cNodeToLine.get(to).get("id").getAsInt();
 
 		if (from.getType() != ConversationNode.OPTION) {
 			// make link from the last line of the "from" node
 			sourceId += from.getLineCount()-1;
 		} else {
 			// outgoing option nodes use multiple targets
 			sourceId += i;
 		}
 
 		out = idToLine.get(sourceId).getAsJsonArray("outgoing");
 		if (to.getType() == ConversationNode.OPTION) {
 			// incoming links into option nodes must go to each line
 			for (int j=0; j<to.getChildCount(); j++) {
 				out.add(new JsonPrimitive(targetId + j));
 			}
 		} else {
 			out.add(new JsonPrimitive(targetId));
 		}
 	}
 
 	public String toJson() {
 		return new GsonBuilder().setPrettyPrinting().create().toJson(root);
 	}
 }
