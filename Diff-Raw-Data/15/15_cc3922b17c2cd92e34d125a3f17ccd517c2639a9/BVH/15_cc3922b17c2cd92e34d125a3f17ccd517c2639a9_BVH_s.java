 /*
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * Copyright (C) 2006 by Vinay Pulim.
  * All rights reserved.
  *
  */
 
 package org.tavatar.tavimator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Writer;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.util.Log;
 
 public class BVH {
 	public static String TAG = "BVH";
 
 	public int lastLoadedNumberOfFrames;
 	public int lastLoadedLoopIn;
 	public int lastLoadedLoopOut;
 	public float lastLoadedAvatarScale;
 	public float lastLoadedFrameTime;
 	public Animation.FigureType lastLoadedFigureType;
 	public BVHNode lastLoadedPositionNode;
 
 	public class NodeCounter {
 		int nodeCount = 0;
 	}
 
 	public List<Rotation> rotationCopyBuffer = new ArrayList<Rotation>();
 	public Position positionCopyBuffer;
 
 	private String inputFile;
 	private String[] tokens;
 	private int tokenPos;
 
 	// remember if the loaded animation is in old or new AVM format
 	private boolean havePositionKeys;
 
 	private List<String> validNodes = new ArrayList<String>();
 	private BVHNode positionNode;
 
 	private int pasteIndex;
 
 	public BVH() {
 		Collections.addAll(validNodes,
 				"hip", "abdomen", "chest", "neck", "head",
 				"lCollar", "lShldr", "lForeArm", "lHand",
 				"rCollar", "rShldr", "rForeArm", "rHand",
 				"lThigh", "lShin", "lFoot",
 				"rThigh", "rShin", "rFoot",
 				"Site");
 	}
 
 	public BVHNode bvhRead(String file) throws IOException {
 		Log.d(TAG, "BVH.bvhRead('" + file + "')");
 		return bvhRead(openFileNamed(file, "BVH"));
 	}
 
 	public BVHNode bvhRead(InputStream file) throws IOException {
 		return bvhRead(new InputStreamReader(file));
 	}
 
 	public BVHNode bvhRead(Reader animationFile) throws IOException {
 		char[] buffer = new char[4096];
 		StringBuilder contents = new StringBuilder();
 		int readLength;
		while(animationFile.ready()) {
			readLength = animationFile.read(buffer, 0, buffer.length);
 			contents.append(buffer, 0, readLength);
 		}
 		animationFile.close();
 
 		inputFile = contents.toString().trim().replaceAll("\\s+", " ");
 		tokens=inputFile.split(" ");
 
 		expect_token("HIERARCHY");
 
 		BVHNode root=bvhReadNode();
 
 		expect_token("MOTION");
 		expect_token("Frames:");
 		int totalFrames=Integer.parseInt(token());
 		lastLoadedNumberOfFrames=totalFrames;
 
 		expect_token("Frame");
 		expect_token("Time:");
 
 		// store FPS
 		lastLoadedFrameTime=Float.parseFloat(token());
 
 		for(int i=0;i<totalFrames;i++)
 			assignChannels(root,i);
 
 		setAllKeyFramesHelper(root,totalFrames);
 
 		return(root);
 	}
 
 
 	//public void assignChannels(BVHNode* node, FILE* f, int frame);
 
 	public void setChannelLimits(BVHNode node, BVHChannelType type, float min, float max) {
 		Log.d(TAG, "BVH.setChannelLimits()");
 
 		int i;
 		if(node == null) return;
 		for(i = 0; i < node.numChannels; i++) {
 			if(node.channelType[i] == type) {
 				node.channelMin[i] = min;
 				node.channelMax[i] = max;
 				return;
 			}
 		}
 	}
 
 	//read joint limits file
 	public void parseLimFile(BVHNode root, String limFile) throws IOException {
 		Log.d(TAG, "BVH.parseLimFile('" + limFile + "')");
 		parseLimFile(root, new BufferedReader(openFileNamed(limFile, "Limits")));
 	}
 
 	public void parseLimFile(BVHNode root, InputStream limFile) throws IOException {
 		parseLimFile(root, new BufferedReader(new InputStreamReader(limFile)));
 	}
 
 	public void parseLimFile(BVHNode root, BufferedReader limit) throws IOException {
 		try {
			while(limit.ready()) {

				String line = limit.readLine().trim();

 				String[] parameters = line.split(" ");
 				String name = parameters[0];
 				float weight = Float.parseFloat(parameters[1]);
 
 				BVHNode node = bvhFindNode(root, name);
 				if(node != null) {
 					node.ikWeight = weight;
 
 					for(int i=0;i<3;i++) {
 						String channel=parameters[i*3+2];
 						float min=Float.parseFloat(parameters[i*3+3]);
 						float max=Float.parseFloat(parameters[i*3+4]);
 
 						if     (channel.startsWith("X")) setChannelLimits(node, BVHChannelType.BVH_XROT, min, max);
 						else if(channel.startsWith("Y")) setChannelLimits(node, BVHChannelType.BVH_YROT, min, max);
 						else if(channel.startsWith("Z")) setChannelLimits(node, BVHChannelType.BVH_ZROT, min, max);
 					} // for
 				} else {
 					Log.d(TAG, "BVH.parseLimFile(): Node '" + name + "' not in animation. This will lead to problems!");
 				}
 			}
 		} catch (IOException e) {
 			IOException e2 = new IOException("Error reading limits file.");
 			e2.initCause(e);
 			throw e2;
 		} finally {
 			limit.close();
 		}
 	}
 
 
 	// public void setNumFrames(int numFrames);
 
 	// public int numFrames() const;
 
 	public void setAllKeyFrames(Animation anim) {
 		Log.d(TAG, "BVH.setAllKeyFrames()");
 
 		setAllKeyFramesHelper(anim.getMotion(), anim.getNumberOfFrames());
 	}
 
 	public void bvhIndent(Writer out, int depth) throws IOException {
 		for(int i=0;i<depth;i++)
 			out.write('\t');
 	}
 
 	public void bvhWriteNode(BVHNode node, Writer out, int depth) throws IOException {
 		bvhIndent(out,depth);
 		out.write(node.type.name);
 		out.write(' ');
 		out.write(node.name());
 		out.write('\n');
 
 		bvhIndent(out, depth);
 		out.write("{\n");
 
 		bvhIndent(out, depth+1);
 		out.write("OFFSET ");
 		out.write(String.valueOf(node.offset[0]));
 		out.write(' ');
 		out.write(String.valueOf(node.offset[1]));
 		out.write(' ');
 		out.write(String.valueOf(node.offset[2]));
 		out.write('\n');
 
 		if(node.type != BVHNodeType.BVH_END) {
 			bvhIndent(out,depth+1);
 			out.write("CHANNELS ");
 			out.write(String.valueOf(node.numChannels));
 			out.write(' ');
 			for (int i = 0; i < node.numChannels; i++){
 				out.write(node.channelType[i].name);
 				out.write(' ');
 			}
 
 			out.write('\n');
 		}
 		for(int i = 0; i < node.numChildren(); i++) {
 			bvhWriteNode(node.child(i), out, depth+1);
 		}
 
 		bvhIndent(out,depth);
 		out.write("}\n");
 	}
 
 	public void bvhWriteFrame(BVHNode node, Writer out, int frame) throws IOException {
 		Rotation rot = node.frameData(frame).rotation();
 		Position pos = positionNode.frameData(frame).position();
 
 		// preserve channel order while writing
 		for (int i = 0; i < node.numChannels; i++) {
 			float value=0.0f;
 			BVHChannelType type = node.channelType[i];
 
 			if     (type == BVHChannelType.BVH_XPOS) value=pos.x;
 			else if(type == BVHChannelType.BVH_YPOS) value=pos.y;
 			else if(type == BVHChannelType.BVH_ZPOS) value=pos.z;
 
 			else if(type == BVHChannelType.BVH_XROT) value=rot.x;
 			else if(type == BVHChannelType.BVH_YROT) value=rot.y;
 			else if(type == BVHChannelType.BVH_ZROT) value=rot.z;
 
 			out.write(String.valueOf(value));
 			out.write(' ');
 		}
 
 		for(int i = 0; i < node.numChildren(); i++)
 			bvhWriteFrame(node.child(i),out,frame);
 	}
 
 	public void bvhPrintNode(BVHNode n, int depth) {
 		int i;
 		String indent = "";
 		for(i = 0; i < depth; i++)
 			indent += "    ";
 		Log.d(TAG, indent + n.name() + " (" + n.offset[0] + n.offset[1] + n.offset[2] + ")");
 		for(i = 0; i < n.numChildren(); i++)
 			bvhPrintNode(n.child(i), depth+1);
 	}
 
 
 	public void bvhWrite(Animation anim, String file) throws IOException {
 		Writer out = new BufferedWriter(new FileWriter(file));
 
 		//out.setNumberFlags(QTextStream.ForcePoint);
 		//out.setRealNumberPrecision(7);
 
 		BVHNode root = anim.getMotion();
 		positionNode = anim.getNode(0);
 
 		out.write("HIERARCHY\n");
 		bvhWriteNode(root, out, 0);
 
 		out.write("MOTION\n");
 
 		out.write("Frames:\t");
 		out.write(String.valueOf(anim.getNumberOfFrames()));
 		out.write('\n');
 		out.write("Frame Time:\t");
 		out.write(String.valueOf(anim.frameTime()));
 		out.write('\n');
 
 		for(int i = 0; i < anim.getNumberOfFrames(); i++) {
 			bvhWriteFrame(root, out, i);
 			out.write('\n');
 		}
 
 		out.close();
 	}
 
 	public BVHNode bvhFindNode(BVHNode root, String name) {
 		BVHNode node;
 		if(root == null) return null;
 		if(root.name().equals(name)) return root;
 
 		for(int i = 0; i < root.numChildren(); i++) {
 			if((node = bvhFindNode(root.child(i), name)) != null) return node;
 		}
 
 		return null;
 	}
 
 
 	public ChannelLimits bvhGetChannelLimits(BVHNode node, BVHChannelType type) {
 		if(node == null) return new ChannelLimits(-10000, 10000);
 
 		for(int i = 0; i < node.numChannels; i++) {
 			if(node.channelType[i]==type) {
 				return new ChannelLimits(node.channelMin[i], node.channelMax[i]);
 			}
 		}
 
 		// qavimator originally never checked to see if this happened, and would leave the return values unititialized if so.
 		Log.d(TAG, "bvhGetChannelLimits: unknown channel type " + type.name + " for node " + node.name());
 		return new ChannelLimits(-10000, 10000);
 	}
 
 	public void bvhResetIK(BVHNode root) {
 		if(root == null) return;
 
 		root.ikOn = false;
 		for(int i = 0; i < root.numChildren(); i++) {
 			bvhResetIK(root.child(i));
 		}
 	}
 
 
 	public String bvhGetName(BVHNode node, int index) {
 		return bvhGetNameHelper(node, index, new NodeCounter());
 	}
 
 	public int bvhGetIndex(BVHNode node, String name) {
 		return bvhGetIndexHelper(node, name, new NodeCounter());
 	}
 
 	public void bvhCopyOffsets(BVHNode dst, BVHNode src) {
 		if(dst == null || src == null) return;
 
 		dst.offset[0] = src.offset[0];
 		dst.offset[1] = src.offset[1];
 		dst.offset[2] = src.offset[2];
 		for(int i = 0; i < src.numChildren(); i++) {
 			bvhCopyOffsets(dst.child(i), src.child(i));
 		}
 	}
 
 
 	public void bvhGetFrameData(BVHNode node, int frame) {
 		if(node == null) return;
 
 		rotationCopyBuffer.clear();
 		positionCopyBuffer = lastLoadedPositionNode.frameData(frame).position();
 		bvhGetFrameDataHelper(node,frame);
 	}
 
 	public void bvhSetFrameData(BVHNode node, int frame) {
 		if(node == null) return;
 
 		// reset paste buffer counter
 		pasteIndex=0;
 		lastLoadedPositionNode.addKeyframe(frame, positionCopyBuffer, new Rotation());
 		// paste all keyframes for all joints
 		bvhSetFrameDataHelper(node,frame);
 	}
 
 	private static Reader openFileNamed(String file, String type) throws FileNotFoundException {
 		FileReader animationFile;
 		try {
 			animationFile = new FileReader(file);
 		} catch(FileNotFoundException e) {
 			FileNotFoundException e2 = new FileNotFoundException(type + " file not found at: " + file);
 			e2.initCause(e);
 			throw e2;
 		}
 		return animationFile;
 	}
 	
 	public static Reader openBVHFile(String animationFileName) throws FileNotFoundException {
 		// rudimentary file type identification from filename
 		if(animationFileName.toLowerCase().endsWith(".bvh")) {
 			return new BufferedReader(openFileNamed(animationFileName, "BVH"));
 		} else if(animationFileName.toLowerCase().endsWith(".avm")) {
 			return new BufferedReader(openFileNamed(animationFileName, "AVM"));
 		} else {
 			return null;
 		}
 	}
 
 	public static Reader openBVHFile(InputStream i) {
 		return bufferedReaderOnStream(i);
 	}
 
 	public static boolean isAvm(String animationFileName) {
 		// rudimentary file type identification from filename
 		if(animationFileName.toLowerCase().endsWith(".bvh")) {
 			return false;
 		} else if(animationFileName.toLowerCase().endsWith(".avm")) {
 			return true;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	private static Reader readerOnStream(InputStream i) {
 		if (i == null) return null;
 		return new InputStreamReader(i);
 	}
 
 	private static BufferedReader bufferedReaderOnStream(InputStream i) {
 		if (i == null) return null;
 		return new BufferedReader(readerOnStream(i));
 	}
 
 	// lex neva's stuff:
 	public BVHNode animRead(String animationFileName, String limitsFileName) throws IOException {
 		BufferedReader limitsFile = null;
 
 		if(limitsFileName.length() != 0) {
 			limitsFile = new BufferedReader(openFileNamed(limitsFileName, "Limits"));
 		}
 
 		return animRead(animationFileName, limitsFile);
 	}
 
 	public BVHNode animRead(String fileName, InputStream limFile) throws IOException {
 		return animRead(fileName, bufferedReaderOnStream(limFile));
 	}
 
 	public BVHNode animRead(String animationFileName, BufferedReader limitsFile) throws IOException {
 		Reader animationFile = null;
 		boolean isAvm;
 
 		// rudimentary file type identification from filename
 		if(animationFileName.toLowerCase().endsWith(".bvh")) {
 			animationFile = openFileNamed(animationFileName, "BVH");
 			isAvm = false;
 		} else if(animationFileName.toLowerCase().endsWith(".avm")) {
 			animationFile = openFileNamed(animationFileName, "AVM");
 			isAvm = true;
 		} else {
 			return null;
 		}
 
 		return animRead(animationFile, limitsFile, isAvm);
 	}
 
 	public BVHNode animRead(InputStream file, InputStream limFile, boolean isAvm) throws IOException {
 		return animRead(readerOnStream(file), bufferedReaderOnStream(limFile), isAvm);
 	}
 
 	public BVHNode animRead(Reader file, InputStream limFile, boolean isAvm) throws IOException {
 		return animRead(file, bufferedReaderOnStream(limFile), isAvm);
 	}
 
 	public BVHNode animRead(Reader file, BufferedReader limFile, boolean isAvm) throws IOException {
 		BVHNode root;
 
 		// positions pseudonode
 		lastLoadedPositionNode = new BVHNode("position");
 		lastLoadedPositionNode.type = BVHNodeType.BVH_POS;
 		// default avatar scale for BVH and AVM files
 		lastLoadedAvatarScale = 1.0f;
 		// default figure type
 		lastLoadedFigureType = Animation.FigureType.FIGURE_FEMALE;
 		// indicates "no loop points set"
 		lastLoadedLoopIn = -1;
 		// Log.d(TAG, "BVH.animRead(): set loop in to -1 to indicate missing loop points");
 		// reset token position
 		tokenPos = 0;
 
 		// assume old style animation format for compatibility
 		havePositionKeys = false;
 		// rudimentary file type identification from filename
 		if(!isAvm) {
 			root = bvhRead(file);
 		} else { 
 			root = avmRead(file);
 		}
 
 		if(limFile != null)
 			parseLimFile(root,limFile);
 
 		removeNoSLNodes(root);
 		// dumpNodes(root,QString.null);
 
 		// old style animation or BVH format means we need to add position keyframes ourselves
 		if(!havePositionKeys) {
 			for(int index = 0; index < root.numKeyframes(); index++) {
 				final FrameData frameData = root.keyframeDataByIndex(index);
 				int frameNum = frameData.frameNumber();
 				lastLoadedPositionNode.addKeyframe(frameNum, frameData.position(), new Rotation());
 				lastLoadedPositionNode.setEaseIn(frameNum, frameData.easeIn());
 				lastLoadedPositionNode.setEaseOut(frameNum, frameData.easeOut());
 			}
 		}
 
 		return root;
 	}
 
 	public BVHNode avmRead(String file) throws IOException {
 		Log.d(TAG, "BVH.avmRead(" + file + ")");
 		return avmRead(openFileNamed(file, "AVM"));
 	}
 
 	public BVHNode avmRead(InputStream file) throws IOException {
 		return avmRead(new InputStreamReader(file));
 	}
 
 	public BVHNode avmRead(Reader animationFile) throws IOException {
 		char[] buffer = new char[4096];
 		StringBuilder contents = new StringBuilder();
 		int readLength;
		while(animationFile.ready()) {
			readLength = animationFile.read(buffer, 0, buffer.length);
 			contents.append(buffer, 0, readLength);
 		}
 		animationFile.close();
 
 		inputFile = contents.toString().trim().replaceAll("\\s+", " ");
 		tokens = inputFile.split(" ");
 
 		expect_token("HIERARCHY");
 
 		BVHNode root = bvhReadNode();
 
 		expect_token("MOTION");
 		expect_token("Frames:");
 		int totalFrames = Integer.parseInt(token());
 		lastLoadedNumberOfFrames = totalFrames;
 		lastLoadedLoopOut = totalFrames;
 		// Log.d(TAG, "BVH.avmRead(): set loop out to totalFrames");
 
 		expect_token("Frame");
 		expect_token("Time:");
 
 		// set FPS
 		lastLoadedFrameTime = Float.parseFloat(token());
 
 		for(int i=0;i<totalFrames;i++) {
 			assignChannels(root,i);
 		}
 
 		avmReadKeyFrame(root);
 
 		if(expect_token("Properties"))
 			avmReadKeyFrameProperties(root);
 
 		// read remaining properties
 		String propertyName;
 		while((propertyName=token()).length() != 0) {
 			String propertyValue=token();
 
 			if(propertyValue.length() != 0) {
 				Log.d(TAG, "BVH.avmRead(): Found extended property: '" + propertyName + "=" + propertyValue + "'");
 				if(propertyName.equals("Scale:")) {
 					lastLoadedAvatarScale = Float.parseFloat(propertyValue);
 				} else if(propertyName.equals("Figure:")) {
 					lastLoadedFigureType = Animation.FigureType.values()[Integer.parseInt(propertyValue)];
 				} else if(propertyName.equals("LoopIn:")) {
 					//             Log.d(TAG, "BVH.avmRead(): set loop in to "+propertyValue);
 					lastLoadedLoopIn = Integer.parseInt(propertyValue);
 				} else if(propertyName.equals("LoopOut:")) {
 					//             Log.d(TAG, "BVH.avmRead(): set loop out to "+propertyValue);
 					lastLoadedLoopOut = Integer.parseInt(propertyValue);
 				} else if(propertyName.equals("Positions:")) {
 					// remember that this is a new animation that has seperate position keyframes
 					havePositionKeys = true;
 
 					int num = Integer.parseInt(propertyValue);
 					Log.d(TAG, "Reading " + num + " Positions:");
 					for(int index = 0; index < num; index++) {
 						int key = Integer.parseInt(token());
 						Log.d(TAG, "Reading position frame " + key);
 						FrameData frameData = root.frameData(key);
 						lastLoadedPositionNode.addKeyframe(key, frameData.position(), new Rotation());
 					} // for
 				} else if(propertyName.equals("PositionsEase:")) {
 					int num = Integer.parseInt(propertyValue);
 					Log.d(TAG, "Reading " + num + " PositionsEases:");
 					for(int index=0;index<num;index++) {
 						int key = Integer.parseInt(token());
 						Log.d(TAG, "Reading position ease for key index " + index + ": " + key);
 
 						if((key & 1) == 1) lastLoadedPositionNode.setEaseIn(lastLoadedPositionNode.keyframeDataByIndex(index).frameNumber(), true);
 						if((key & 2) == 2) lastLoadedPositionNode.setEaseOut(lastLoadedPositionNode.keyframeDataByIndex(index).frameNumber(), true);
 
 					} // for
 				} else {
 					Log.d(TAG, "BVH.avmRead(): Unknown extended property '" + propertyName + "' (" + propertyValue + "), ignoring.");
 				}
 			}
 		} // while
 
 		return(root);
 	}
 
 
 
 	public void avmWrite(Animation anim, String file) throws IOException {
 		Writer out = new BufferedWriter(new FileWriter(file));
 
 		//out.setNumberFlags(QTextStream.ForcePoint);
 		//out.setRealNumberPrecision(7);
 
 		BVHNode root = anim.getMotion();
 		positionNode = anim.getNode(0);
 
 		out.write("HIERARCHY\n");
 
 		bvhWriteNode(root, out, 0);
 
 		out.write("MOTION\n");
 
 		out.write("Frames:\t");
 		out.write(String.valueOf(anim.getNumberOfFrames()));
 		out.write('\n');
 
 		out.write("Frame Time:\t");
 		out.write(String.valueOf(anim.frameTime()));
 		out.write('\n');
 		for(int i = 0; i < anim.getNumberOfFrames(); i++) {
 			bvhWriteFrame(root, out, i);
 			out.write('\n');
 		}
 
 		avmWriteKeyFrame(root, out);
 		out.write("Properties\n");
 
 		avmWriteKeyFrameProperties(root,out);
 
 		// write remaining properties
 		out.write("Scale: ");
 		out.write(String.valueOf(anim.getAvatarScale()));
 		out.write('\n');
 
 		out.write("Figure: ");
 		out.write(String.valueOf(anim.getFigureType().ordinal()));
 		out.write('\n');
 
 		out.write("LoopIn: ");
 		out.write(String.valueOf(anim.getLoopInPoint()));
 		out.write('\n');
 
 		out.write("LoopOut: ");
 		out.write(String.valueOf(anim.getLoopOutPoint()));
 		out.write('\n');
 
 		// HACK: add-on for position key support - this needs to be revised badly
 		// HACK: we need a new file format that makes it easier to add new features
 		out.write("Positions: ");
 		avmWriteKeyFrame(positionNode,out);
 
 		out.write("PositionsEase: ");
 		avmWriteKeyFrameProperties(positionNode,out);
 
 		out.close();
 	}
 
 	public void animWrite(Animation anim, String file) throws IOException {
 		// rudimentary file type identification from filename
 		if(file.toLowerCase().endsWith(".bvh"))
 			bvhWrite(anim,file);
 		else if(file.toLowerCase().endsWith(".avm"))
 			avmWrite(anim,file);
 	}
 
 	private String token() {
 		if(tokenPos >= tokens.length) {
 			Log.d(TAG, "BVH.token(): no more tokens at index " + tokenPos);
 			return "";
 		}
 		return tokens[tokenPos++];
 	}
 
 	private boolean expect_token(String name) {
 		// Log.d(TAG, "BVH.expect_token('%s')",name.toLatin1().constData());
 
 		if(!name.equals(token())) {
 			Log.d(TAG, "BVH.expect_token(): Bad or outdated animation file: " + name + " missing");
 			return false;
 		}
 		return true;
 	}
 
 	private BVHNode bvhReadNode() {
 		Log.d(TAG, "BVH.bvhReadNode()");
 
 		String type = token();
 		if(type.equals("}")) return null;
 
 		// check for node type first
 		BVHNodeType nodeType;
 		if      (type.equals("ROOT"))  nodeType = BVHNodeType.BVH_ROOT;
 		else if (type.equals("JOINT")) nodeType = BVHNodeType.BVH_JOINT;
 		else if (type.equals("End"))   nodeType = BVHNodeType.BVH_END;
 		else {
 			Log.d(TAG, "BVH.bvhReadNode(): Bad animation file: unknown node type: '" + type + "'");
 			return null;
 		}
 
 		// add node with name
 		BVHNode node = new BVHNode(token());
 		if(!validNodes.contains(node.name())) {
 			node.type = BVHNodeType.BVH_NO_SL;
 		} else {
 			// set node type
 			node.type = nodeType;
 		}
 
 		expect_token("{");
 		expect_token("OFFSET");
 		node.offset[0] = Float.parseFloat(token());
 		node.offset[1] = Float.parseFloat(token());
 		node.offset[2] = Float.parseFloat(token());
 		node.ikOn = false;
 		node.ikWeight = 0.0f;
 		if(node.type != BVHNodeType.BVH_END) {
 			expect_token("CHANNELS");
 			node.numChannels = Integer.parseInt(token());
 
 			// rotation order for this node
 			String order = "";
 			for (int i = 0; i < node.numChannels; i++) {
 				node.channelMin[i] = -10000;
 				node.channelMax[i] = 10000;
 				type=token();
 				if     (type.equals("Xposition")) node.channelType[i] = BVHChannelType.BVH_XPOS;
 				else if(type.equals("Yposition")) node.channelType[i] = BVHChannelType.BVH_YPOS;
 				else if(type.equals("Zposition")) node.channelType[i] = BVHChannelType.BVH_ZPOS;
 				else if(type.equals("Xrotation")) {
 					node.channelType[i] = BVHChannelType.BVH_XROT;
 					order+='X';
 				} else if(type.equals("Yrotation")) {
 					node.channelType[i] = BVHChannelType.BVH_YROT;
 					order+='Y';
 				} else if(type.equals("Zrotation")) {
 					node.channelType[i] = BVHChannelType.BVH_ZROT;
 					order+='Z';
 				}
 			}
 
 			if     (order.equals("XYZ")) node.channelOrder = BVHOrderType.BVH_XYZ;
 			else if(order.equals("ZYX")) node.channelOrder = BVHOrderType.BVH_ZYX;
 			else if(order.equals("YZX")) node.channelOrder = BVHOrderType.BVH_YZX;
 			else if(order.equals("XZY")) node.channelOrder = BVHOrderType.BVH_XZY;
 			else if(order.equals("YXZ")) node.channelOrder = BVHOrderType.BVH_YXZ;
 			else if(order.equals("ZXY")) node.channelOrder = BVHOrderType.BVH_ZXY;
 		}
 
 		BVHNode child;
 		do {
 			if((child=bvhReadNode()) != null) {
 				node.addChild(child);
 			}
 		} while (child != null);
 
 		return node;
 	}
 
 
 	private void assignChannels(BVHNode node, int frame) {
 		// Log.d(TAG, "BVH.assignChannels()");
 
 		// create new rotation and position objects
 		Rotation rot = new Rotation();
 		Position pos = new Position();
 
 		for(int i = 0; i < node.numChannels; i++) {
 			float value = Float.parseFloat(token());
 			BVHChannelType type=node.channelType[i];
 			if     (type == BVHChannelType.BVH_XPOS) pos.x=value;
 			else if(type == BVHChannelType.BVH_YPOS) pos.y=value;
 			else if(type == BVHChannelType.BVH_ZPOS) pos.z=value;
 			else if(type == BVHChannelType.BVH_XROT) rot.x=value;
 			else if(type == BVHChannelType.BVH_YROT) rot.y=value;
 			else if(type == BVHChannelType.BVH_ZROT) rot.z=value;
 			else Log.d(TAG, "BVH.assignChannels(): unknown channel type " + type);
 		}
 
 		// put rotation and position into the node's cache for later keyframe referencing
 		node.cacheRotation(rot);
 		node.cachePosition(pos);
 
 		for(int i=0;i<node.numChildren();i++)
 			assignChannels(node.child(i),frame);
 	}
 
 
 	private void avmReadKeyFrame(BVHNode root) {
 		// NOTE: new system needs frame 0 as key frame
 		// FIXME: find a better way without code duplication
 		Rotation rot = root.getCachedRotation(0);
 		Position pos = root.getCachedPosition(0);
 		root.addKeyframe(0, new Position(pos.x, pos.y, pos.z), new Rotation(rot.x, rot.y, rot.z));
 		if(root.type == BVHNodeType.BVH_ROOT)
 			lastLoadedPositionNode.addKeyframe(0, new Position(pos.x, pos.y, pos.z), new Rotation(rot.x, rot.y, rot.z));
 
 		int numKeyFrames = Integer.parseInt(token());
 		for(int i = 0; i < numKeyFrames; i++) {
 			int key = Integer.parseInt(token());
 
 			if(key<lastLoadedNumberOfFrames) {
 				rot = root.getCachedRotation(key);
 				pos = root.getCachedPosition(key);
 				root.addKeyframe(key, new Position(pos.x, pos.y, pos.z), new Rotation(rot.x, rot.y, rot.z));
 			}
 		}
 
 		// all keyframes are found, flush the node's cache to free up memory
 		root.flushFrameCache();
 		// root.dumpKeyframes();
 
 		for(int i = 0; i < root.numChildren(); i++)
 			avmReadKeyFrame(root.child(i));
 	}
 
 	//reads ease in / out data
 	private void avmReadKeyFrameProperties(BVHNode root) {
 		// NOTE: key frame properties save key 0, too, so numKeyFrames here will be one higher than before
 
 		Log.d(TAG, "BVH.avmReadKeyFrameProperties()");
 
 		String numKeys = token();
 		if(numKeys.length() == 0) return;
 
 		int numKeyFrames = Integer.parseInt(numKeys);
 		Log.d(TAG, "BVH.avmReadKeyFrameProperties(): reading properties for " + numKeyFrames + " key frames");
 
 		for(int i=0;i<numKeyFrames;i++)
 		{
 			int key = Integer.parseInt(token());
 
 			if((key & 1) == 1) root.setEaseIn(root.keyframeDataByIndex(i).frameNumber(),true);
 			if((key & 2) == 2) root.setEaseOut(root.keyframeDataByIndex(i).frameNumber(),true);
 		}
 
 		for(int i=0;i<root.numChildren();i++)
 			avmReadKeyFrameProperties(root.child(i));
 	}
 
 
 	/* .avm files look suspiciously like .bvh files, except
     with keyframe data tacked at the end -- Lex Neva */
 	private void avmWriteKeyFrame(BVHNode root, Writer out) throws IOException {
 		Integer[] keys = root.keyframeList();
 		// no key frames (usually at joint ends), just write a 0\n line
 		if(keys.length == 0) {
 			out.write("0\n");
 		} else { // write line of key files
 			// write number of key files
 			out.write(String.valueOf(keys.length-1));
 			out.write(' ');
 
 			// skip frame 0 (always key frame) while saving and write all keys in a row
 			for (int i = 1; i < keys.length; i++) {
 				out.write(String.valueOf(keys[i]));
 				out.write(' ');
 			}
 
 			out.write('\n');
 		}
 
 		for(int i=0;i<root.numChildren();i++)
 			avmWriteKeyFrame(root.child(i),out);
 	}
 
 	//writes ease in / out data
 	private void avmWriteKeyFrameProperties(BVHNode root, Writer out) throws IOException {
 		Integer[] keys=root.keyframeList();
 
 		// NOTE: remember, ease in/out data always takes first frame into account
 		out.write(String.valueOf(keys.length));
 		out.write(' ');
 
 		// NOTE: remember, ease in/out data always takes first frame into account
 		for (int i = 0; i < keys.length; i++) {
 			int type=0;
 
 			if(root.keyframeDataByIndex(i).easeIn()) type|=1;
 			if(root.keyframeDataByIndex(i).easeOut()) type|=2;
 
 			out.write(String.valueOf(type));
 			out.write(' ');
 		}
 		out.write('\n');
 
 		for(int i=0;i<root.numChildren();i++)
 			avmWriteKeyFrameProperties(root.child(i),out);
 	}
 
 
 	// removes all unknown nodes from the animation
 	private void removeNoSLNodes(BVHNode root) {
 		Log.d(TAG, "BVH.removeNoSLNodes()");
 		// walk through list of all child nodes
 		for (int i = 0; i < root.numChildren(); i++) {
 			BVHNode child = root.child(i);
 			// if this is an unsupported node, remove it
 			if (child.type == BVHNodeType.BVH_NO_SL) {
 				Log.d(TAG, "BVH.removeNoSLNodes(): removing node '" + child.name() + "'");
 				// find all child joints of the unsupported child
 				for(int j = 0; j < child.numChildren(); j++) {
 					// move child nodes to the current parent joint
 					root.insertChild(child.child(j), i);
 				}
 				// remove unsupported child node
 				root.removeChild(child);
 
 				// start checking for nodes at this point over again
 				removeNoSLNodes(root);
 				return;
 			}
 			// check next parent node
 			removeNoSLNodes(root.child(i));
 		}
 	}
 
 
 	// debugging function, dumps the node structure
 	//for debugging only
 	public void dumpNodes(BVHNode node, String indent) {
 		Log.d(TAG, indent + " " + node.name() + " (" + node.numChildren() + ")");
 		indent += "+--";
 		for(int i = 0; i < node.numChildren(); i++) {
 			dumpNodes(node.child(i), indent);
 		}
 	}
 
 
 	//in BVH files, this is necessary so that
 	//all frames but the start and last aren't
 	//blown away by interpolation
 	private void setAllKeyFramesHelper(BVHNode node, int numberOfFrames) {
 		// Log.d(TAG, "BVH.setAllKeyFramesHelper()");
 
 		for(int i = 0; i < numberOfFrames; i++) {
 			final Rotation rot = node.getCachedRotation(i);
 			final Position pos = node.getCachedPosition(i);
 
 			if (node.type != BVHNodeType.BVH_END)
 				node.addKeyframe(i, new Position(pos.x, pos.y, pos.z), new Rotation(rot.x, rot.y, rot.z));
 		}
 
 		for(int i = 0; i < node.numChildren(); i++)
 			setAllKeyFramesHelper(node.child(i),numberOfFrames);
 	}
 
 	private String bvhGetNameHelper(BVHNode node,int index,NodeCounter nodeCounter) {
 		nodeCounter.nodeCount++;
 		if(nodeCounter.nodeCount==index) return node.name();
 
 		for(int i = 0; i < node.numChildren(); i++) {
 			final String val=bvhGetNameHelper(node.child(i),index,nodeCounter);
 			if(val.length() != 0) return val;
 		}
 		return "";
 	}
 
 	private int bvhGetIndexHelper(BVHNode node, String name, NodeCounter nodeCounter) {
 		nodeCounter.nodeCount++;
 		if(node.name().equals(name)) return nodeCounter.nodeCount;
 
 		for(int i = 0; i < node.numChildren(); i++) {
 			int val;
 			if((val = bvhGetIndexHelper(node.child(i), name, nodeCounter)) != 0)
 				return val;
 		}
 		return 0;
 	}
 
 
 	private void bvhGetFrameDataHelper(BVHNode node, int frame) {
 		if (node.type != BVHNodeType.BVH_END) {
 			rotationCopyBuffer.add(node.frameData(frame).rotation());
 
 			//   rotationCopyBuffer[rotationCopyBuffer.count()-1].bodyPart=node.name(); // not necessary but nice for debugging
 			//   Log.d(TAG, QString("copying frame data for %1 frame number %2 (%3)")
 			//             .arg(node.name()).arg(frame).arg(positionCopyBuffer[rotationCopyBuffer.count()-1].bodyPart));
 		}
 
 
 		for(int i=0;i<node.numChildren();i++)
 			bvhGetFrameDataHelper(node.child(i),frame);
 	}
 
 	private void bvhSetFrameDataHelper(BVHNode node, int frame) {
 		// if this node is not the end of a joint child structure
 		if(node.type != BVHNodeType.BVH_END) {
 			// add the node as key frame
 			node.addKeyframe(frame, new Position(), rotationCopyBuffer.get(pasteIndex));
 			//   Log.d(TAG, QString("pasting frame data for %1 frame number %2 (%3)").arg(node.name()).arg(frame).arg(rotationCopyBuffer[pasteIndex].bodyPart));
 			// increment paste buffer counter
 			pasteIndex++;
 		}
 
 		// traverse down the child list, call this function recursively
 		for(int i = 0; i < node.numChildren(); i++)
 			bvhSetFrameDataHelper(node.child(i),frame);
 	}
 }
