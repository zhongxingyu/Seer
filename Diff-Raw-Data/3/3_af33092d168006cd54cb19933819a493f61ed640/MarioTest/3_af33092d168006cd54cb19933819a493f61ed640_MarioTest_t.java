 /*
  * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Mario AI nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package project6867;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import net.sf.javaml.classification.Classifier;
 import project6867.ClassifierTrainer.ClassifierType;
 import project6867.ClassifierTrainer.DataType;
 import ch.idsia.agents.Agent;
 import ch.idsia.agents.LearningAgent;
 import ch.idsia.benchmark.tasks.BasicTask;
 import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
 import ch.idsia.tools.EvaluationInfo;
 import ch.idsia.tools.MarioAIOptions; import erekspeed.CuckooSubFBJTAAgent;
 import erekspeed.ErekSpeedCuckooAgent;
 /**
  * Created by IntelliJ IDEA.
  * User: julian
  * Date: May 5, 2009
  * Time: 12:46:43 PM
  */
 
 
 
 public final class MarioTest
 {
 
 	private static MarioAIOptions options;
 	private static BufferedWriter output;
 	
 	public static void evaluate(Agent agent) {
 		
 		options.setAgent(agent);
 		final BasicTask basicTask = new BasicTask(options);
 		//options.setVisualization(true);
 
 		basicTask.doEpisodes(1, false, 1);
 		EvaluationInfo info = basicTask.getEnvironment().getEvaluationInfo();
 		try {
 			output.write("{" + options.asString() + "} "
 					+ info.computeWeightedFitness() + ", "
 					+ info.marioStatus + "\n");
 			output.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println("\nEvaluationInfo: \n" + basicTask.getEnvironment().getEvaluationInfoAsString());
 	}
 
 
 	public static void main(String[] args)
 	{
 		try{output = new BufferedWriter(new FileWriter("NB1k@.1results.txt"));}catch(IOException e){e.printStackTrace();} 
 		Classifier c = ClassifierTrainer.getClassifier(ClassifierType.NB, DataType.ONE, null);
 		Agent agent = new MLAgent(c);
 		String[] ops = {
 				"-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg off -le off -ls 98886", // no enemies, no blocks, no gaps
 				"-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg off -le on -ls 31646", // enemies
 				"-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg on -le off -ls 16007", // just gaps
 				"-vis off -ll 256 -lb on -lco off -lca off -ltb off -lg off -le on -ls 19682", //enemies, blocks
 				"-vis off -ll 256 -lb on -lco off -lca off -ltb off -lg on -ls 79612"}; // enemies, blocks, gaps
 
 		for(int diff = 1; diff < 10; diff++){
 	    	for(String o : ops){
	    		options = new MarioAIOptions(args);
 	    		options.setArgs(o);
 	    		options.setLevelDifficulty(diff);
 	    		evaluate(agent);
 	    	}
 	    }		
 		try{output.close();}catch(IOException e){e.printStackTrace();}
 		System.exit(0);
 	}
 }
