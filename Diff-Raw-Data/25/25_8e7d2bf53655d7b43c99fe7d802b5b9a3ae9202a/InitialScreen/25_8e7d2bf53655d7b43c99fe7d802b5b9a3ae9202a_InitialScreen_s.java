 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package initialScreen;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.OverlayLayout;
 
 /**
  *
  * @author soriano
  */
 public class InitialScreen {
 
 	static Integer [] agents;
 	static int mode = 0;
 	static ImageDrag imageDrag = new ImageDrag();         
 	static JPanel pane = new JPanel();
 	static fixedImage ringBlack = new fixedImage("../data/img/ring2.png", 10, 400);
 	static fixedImage ringWhite = new fixedImage("../data/img/ring2.png", 410, 400);         
 
 	static JButton continueButton = new javax.swing.JButton("Continue");
 	static JButton playButton = new javax.swing.JButton("Play!");
 	static fixedImage background = new fixedImage("../data/img/backgroundSmall.JPG", 0, 0);
 	static JFrame frame = new JFrame("Demo");
 
 	public static void main(String[] args) {         
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(800, 775);
 
 		frame.add(pane);         
 		pane.setLayout(new OverlayLayout(pane));
 
 		continueButton.setAlignmentX(800);
 		continueButton.setAlignmentY(775);
 
 		continueButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent evt)
 			{
 				// ... called when button clicked
 				agents = imageDrag.getAgentsWhite();
 
 				mode = 1;
 
 				pane.remove(imageDrag);
 				pane.remove(ringBlack);
 				pane.remove(ringWhite);
 				pane.remove(continueButton);
 				pane.remove(background);
 
 				for(int i = 0; i < agents.length; i++)
 					System.out.println(agents[i]);
 
 				pane.repaint();
 
 				selectWeights();
 			}
 		});
 
 		pane.add(imageDrag);
 		/*pane.add(mogo);
 		 pane.add(pachi);
          pane.add(gnugo);
          pane.add(fuegoStar);
          pane.add(fuegoSquare);*/
 		pane.add(ringBlack);
 		pane.add(ringWhite);         
 		pane.add(continueButton);
 		pane.add(background);
 
 		frame.setVisible(true);                                     
 	}
 
 	static public void selectWeights()
 	{
 		final DragBars dragBars = new DragBars(agents);
 
 		pane.add(dragBars);
 
 		playButton.setAlignmentX(800);
 		playButton.setAlignmentY(775);
 
 		playButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent evt)
 			{                
 				String softwares[] = {"/home/soriano/pesquisa/desenvolvimento/go/fuego-1.1c/fuegomain/fuego --nobook --config /home/soriano/pesquisa/desenvolvimento/go/configGroupGo/configSlowOneThread",
 						"/usr/games/gnugo --mode gtp --level 15",
 						"/home/soriano/pesquisa/desenvolvimento/go/fuego-1.1-mine2/fuegomain/fuego --nobook --config /home/soriano/pesquisa/desenvolvimento/go/configGroupGo/configSlowOneThread-ag5",
 						"/home/soriano/pesquisa/desenvolvimento/go/MoGo_release3/mogo",
 						"/home/soriano/pesquisa/desenvolvimento/go/fuego-1.1-mine/fuegomain/fuego --nobook --config /home/soriano/pesquisa/desenvolvimento/go/configGroupGo/configSlowOneThread-ag5",
 				"/home/soriano/pesquisa/desenvolvimento/go/fuego-1.1-mine2/fuegomain/fuego --nobook --config /home/soriano/pesquisa/desenvolvimento/go/configGroupGo/configSlowOneThread-ag5"};
 				String expertsImg[] = {"fuego.png", "gnugo.png", "pachi.png", "mogo.png", "fuegoStar.png", "fuegoSquare.png"};
 
 				String gogui = "../gogui/bin/gogui";
 				String twogtp = "../gogui/bin/gogui-twogtp";
 
 				// ... called when button clicked
 				Float [] weights = dragBars.getWeights();                                

 				try {
 					FileWriter fstreamExperts = new FileWriter("../gogui/experts.txt");
 					BufferedWriter outExperts = new BufferedWriter(fstreamExperts);

					int validAgents = 0;
 					for(int i = 0; i < agents.length; i++)
 					{
 						if(agents[i] != -1)
 						{
 							outExperts.write(expertsImg[agents[i]]+"\n");
 							validAgents++;
 						}
 					}
 					
 					outExperts.close();
 
 					//FileWriter fstream = new FileWriter("/home/soriano/pesquisa/desenvolvimento/go/groupGoSimpleDemo/execute.sh");
 					BufferedWriter out = new BufferedWriter(new FileWriter("../gogui/gogui_script.sh"));
 					out.write("#!/bin/sh\n");
 					out.write("BLACK=\"" + softwares[agents[0]] + "\"\n");
 					out.write("WHITE=\"" + softwares[agents[0]] + "\"\n");
					//out.write("ALICE=\"" + softwares[agents[0]] + "\"\n");
					//out.write("ALBERT=\"" + softwares[agents[1]] + "\"\n");
					//out.write("JOHN=\"" + softwares[agents[2]] + "\"\n");
 					//out.write("TWOGTP=\""+ twogtp + " -black \\\"$BLACK\\\" -white \\\"$WHITE\\\" -alice \\\"$ALICE\\\" -aliceWeight " + Float.toString(weights[0]) + " -albert \\\"$ALBERT\\\" -albertWeight " + Float.toString(weights[1]) + " -john \\\"$JOHN\\\" -johnWeight " + Float.toString(weights[2]) + " -francesco \\\"$FRANCESCO\\\" -francescoWeight " + Float.toString(weights[3]) + " -size 9 -iteration 0 -path /home/soriano/tmp/0/ -initial 0 -games 10 -groupColor white -sgffile /home/soriano/tmp/result\"\n");
 					out.write("TWOGTP=\""+ twogtp + " -black \\\"$BLACK\\\" -white \\\"$WHITE\\\" "
							+ "-numAgents " + (weights.length) + " "
							+ "-agentsList "
							+ "-size 9 -iteration 0 -path ../gogui/output/tmp/0/ -initial 0 -games 3 -groupColor white -sgffile ../gogui/output/tmp/result ");
 					
 					// -path /home/cs102/test_gogui/ -initial 0 -games 5  -groupColor white -sgffile /home/cs102/test_gogui/0/result -size 9" -computer-both
 					
 					// TODO: add agentsList option
 					out.write("-agentsList \\\"");
 					for(int i = 0; i < agents.length; i++)
 					{
 						if(agents[i] != -1)
 						{
 							if(i != 0)
 								out.write('|');
 							out.write(softwares[agents[i]]);
 						}
 					}
 					out.write("\\\" ");
 					
 					// TODO: add weightsList option
 					out.write("-weightsList \\\"");
					for(int i = 0; i < weights.length; i++)
 					{
 						if(i != 0)
 							out.write('|');
 						out.write(new Float(weights[i]).toString());
 					}
 					out.write("\\\" ");
					out.write("\"\n");
					out.write(gogui + " -size 9 -program \"$TWOGTP\"");
 					out.close();
 					
 					BufferedWriter outVotesTextFile = new BufferedWriter(new FileWriter("../gogui/votes_text_file.txt"));
 					outVotesTextFile.write("" + validAgents);
					
 					outVotesTextFile.close();
 
 					FileWriter fstreamFreqAgreed = new FileWriter("../gogui/freqAgreed.txt");
 					BufferedWriter outFreqAgreed = new BufferedWriter(fstreamFreqAgreed);
 					FileWriter fstreamAcceptedOpinions = new FileWriter("../gogui/acceptedOpinions.txt");
 					BufferedWriter outAcceptedOpinions = new BufferedWriter(fstreamAcceptedOpinions);
 
 					for(int i = 0; i < validAgents; i++) {
 						outFreqAgreed.write("0\n");
 						outAcceptedOpinions.write("0\n");
 					}
 
 					outFreqAgreed.close();
 					outAcceptedOpinions.close();
 
 					Runtime.getRuntime().exec("chmod +x ../gogui/gogui_script.sh");
 					Runtime.getRuntime().exec("../gogui/gogui_script.sh", null, new File("../gogui"));
 
 					//Process p = Runtime.getRuntime().exec(gogui + " -size 9");
 					System.exit(1);
 				} catch(Exception e) {
 					System.err.println("Error on exec() method");
 					e.printStackTrace();  
 				}
 			}
 		});
 
 		pane.add(playButton);
 		pane.add(background);
 
 		pane.repaint();                
 
 		frame.setVisible(true);
 
 		playButton.requestFocusInWindow();
 		playButton.requestFocus();
 
 		frame.getRootPane().setDefaultButton(playButton);
 	}
 }
