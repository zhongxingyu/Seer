 package edu.rpi.phil.legup.saveable;
 
 //import java.beans.XMLDecoder;
 //import java.beans.XMLEncoder;
 import java.awt.Point;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 import java.util.Vector;
 //import java.util.zip.GZIPInputStream;
 //import java.util.zip.GZIPOutputStream;
 
 import javax.swing.JOptionPane;
 
 import edu.rpi.phil.legup.BoardState;
 
 public class SaveableProof
 {
 	public SaveableProofState[] states;
 	public Vector<SaveableProofTransition> transitions;
 	public PrintWriter out;
 	public File file;
 	private SaveableProof(BoardState state)
 	{
 		states = new SaveableProofState[state.calcID()];
 		transitions = new Vector<SaveableProofTransition>();
 		//state.makeSaveableProof(states, transitions);
 	}
 	
 	/*private BoardState toBoardState()
 	{
 		BoardState[] bstates = new BoardState[states.length];
 		for(int i = 0; i < states.length; ++i)
 		{
 			bstates[i] = BoardState.fromSaveableProofState(states[i]);
 		}
 		
 		for(SaveableProofTransition t : transitions)
 		{
 			bstates[t.id1].addTransitionFrom(bstates[t.id2], t.justification, t.isCaseRule);
 		}
 		
 		bstates[0].recalculateLocation();
 		
 		return bstates[0];
 	}*/
 	public SaveableProof(){}
 /*LOAD XML	
 	//XML functions
 	
 	public static BoardState loadProof(String filename)
     {
 		SaveableProof rv = null;
     	
     	try
     	{
 	    	XMLDecoder d = new XMLDecoder(
 	                new BufferedInputStream(
 	                		//new GZIPInputStream( //For compression
 	                    new FileInputStream(filename)));
 			Object result = d.readObject();
 			d.close();
 			
 			rv = (SaveableProof)result;
     	}
     	catch (Exception e)
     	{
     		JOptionPane.showMessageDialog(null,"Error loading: " + e.toString());
     		rv = null;
     	}
     	
     	BoardState state = rv.toBoardState(); 
     	
     	if(state == null)
     		JOptionPane.showMessageDialog(null,"Board Load Failed");
     		
     	return state;
     }
 */
 //NORMAL LOAD
 	public static BoardState loadProof(String filename) throws IOException
 	{
 		System.out.println("Loading...\n");
 		BoardState state;
 		Scanner scan = new Scanner(new File(filename));
 		String str;
 		int in;
 		Vector <Integer> labels = new Vector<Integer>();
 		//iterate through file
 		//puzzle
 		str = scan.next();
 		//System.out.println(str);
 		str = scan.next();
 		//str += scan.nextLine();
 		System.out.println(str);
 		//state.setPuzzleName("Battleship");
 		//System.out.println("stupid loadme is working\n");
 		
 		//height
 		scan.next();
 		in = scan.nextInt();
 		
 		//width
 		scan.next();
 		//initialize new board while grabbing width
 		state = new BoardState(in, scan.nextInt());
 		
 		//now that board exists, set name and offset
 		state.setPuzzleName(str);
 		state.setOffset(new Point(0,0));
 		state.setLocation(new Point(0,0));
 		
 		//top labels
 		str = scan.next();
 		//System.out.println(str);
 		str = scan.next();
 		while (!str.equals("bottomLabels:"))
 		{
 			//System.out.println(str);
 			//System.out.flush();
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setTopLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getTopLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//bottom labels
 		str = scan.next();
 		while (!str.equals("leftLabels:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setBottomLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getBottomLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//left labels
 		str = scan.next();
 		while (!str.equals("rightLabels:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setLeftLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getLeftLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//right labels
 		str = scan.next();
 		while (!str.equals("hintCells:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setRightLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getRightLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//hint cells;
 		str = scan.next();
 		
 		//extra data
 		//str = scan.next();
 		
 		//board
 		//scan.next();
 		for (int i = 0; i < state.getHeight(); i++)
 		{
 			for (int j = 0; j < state.getWidth(); j++)
 			{
 				state.getBoardCells()[i][j] = scan.nextInt();
 				//System.out.println(loadme.get(0).boardCells[i][j]);
 			}
 		}
 		System.out.println("board cells loaded...");
 		System.out.flush();
 		
 		//transitions
 		str = scan.next();
 		BoardState currentstate = state;
 		while (scan.hasNext())
 		{
 			str = scan.next();
 			if (str.equals("newState:"))
 			{
 				//add new child to parent
 				currentstate.getTransitionsFrom().add(new BoardState(state));
 				
 				//add parent to child
 				currentstate.getTransitionsFrom().lastElement().getTransitionsTo().add(currentstate);
 
 				//step in to child
 				currentstate = currentstate.getTransitionsFrom().lastElement();
 				
 				//add justification to child
 				scan.nextLine();
 				currentstate.setJustification(scan.nextLine());
				System.out.println("just");
 				
 				//add case rule to child
 				currentstate.setCaseRuleJustification(scan.nextLine());
				System.out.println("case");
 				
 				//add offset to child
 				currentstate.setOffset(new Point(scan.nextInt(), scan.nextInt()));
 				
 				//is this a transition?
 				currentstate.setModifiableState(scan.nextBoolean());
 				
 				//add point changes to child (if they exist)
 				if(scan.hasNextInt())
 				{
 					currentstate.getChangedCells().add(new Point(scan.nextInt(), scan.nextInt()));
 					
 					//change board cell at point
 					currentstate.getBoardCells()[currentstate.getChangedCells().lastElement().y][currentstate.getChangedCells().lastElement().x] = scan.nextInt();
 				}
 			}
 			
 			else if (str.equals("endLeaf:"))
 			{
 				//step out to parent (if we have a parent)
 				if (!currentstate.getTransitionsTo().isEmpty())
 					currentstate = currentstate.getTransitionsTo().lastElement();
 			}
 			
 			//assume str is now the x of the modified cell
 			else
 			{
 				//add point to child
 				currentstate.getChangedCells().add(new Point(Integer.parseInt(str), scan.nextInt()));
 				//change board cell at point
 				currentstate.getBoardCells()[currentstate.getChangedCells().lastElement().y][currentstate.getChangedCells().lastElement().x] = scan.nextInt();
 			}
 		}
 		
 		scan.close();
 		System.out.println("File Closed...");
 		
 		//set first state as unmodifiable
 		for(int i = 0; i < state.getHeight(); i++)
 			for(int j = 0; j < state.getWidth(); j++)
 				state.setModifiableCell(j, i, false);
 		System.out.println("...Done\n");
 		return state;
 	}
 /*SAVE XML	
     public static boolean saveProof(BoardState state, String filename)
     {	
     	SaveableProof s = new SaveableProof(state);
     	
     	try
     	{
 	    	 XMLEncoder e = new XMLEncoder(
 	                 new BufferedOutputStream(
 	                		 //new GZIPOutputStream( //For compression
 	                     new FileOutputStream(filename)));
 			e.writeObject(s);
 			e.close();
     	}
     	catch (Exception e)
     	{
     		JOptionPane.showMessageDialog(null,"Error saving: " + e.getMessage());
     		return false;
     	}
     	
     	return true;
     }
 */
 //NORMAL SAVE
 	public static boolean saveProof(BoardState state, String filename) throws IOException
 	{
 		SaveableProof saveme = new SaveableProof(state);
 		saveme.file = new File(filename);
 		saveme.out = new PrintWriter(new FileWriter(filename));
 		//int[] extradata;
 		//write "static" state data
 		saveme.out.print("Puzzle: ");
 		saveme.out.print(state.getPuzzleName());
 		saveme.out.print("\nHeight: ");
 		saveme.out.print(state.getHeight());
 		saveme.out.print("\nWidth: ");
 		saveme.out.print(state.getWidth());
 		saveme.out.print("\ntopLabels:");
 		for (int savelabel : state.getTopLabels())
 		{
 			saveme.out.print(' ');
 			saveme.out.print(savelabel);
 		}
 		saveme.out.print("\nbottomLabels:");
 		for (int savelabel : state.getBottomLabels())
 		{
 			saveme.out.print(' ');
 			saveme.out.print(savelabel);
 		}
 		saveme.out.print("\nleftLabels:");
 		for (int savelabel : state.getLeftLabels())
 		{
 			saveme.out.print(' ');
 			saveme.out.print(savelabel);
 		}
 		saveme.out.print("\nrightLabels:");
 		for (int savelabel : state.getRightLabels())
 		{
 			saveme.out.print(' ');
 			saveme.out.print(savelabel);
 		}
 		saveme.out.print("\nhintCells:\n");
 		for (Point savehints : state.getHintCells())
 		{
 			//SAVE HINTCELLS
 		}
 		//saveme.out.print("\nextraData:\n");
 		for (Object extra : state.getExtraData())
 		{
 			//SAVE EXTRADATA, ie
 			//extradata = new extra.tosavedata();
 		}
 		saveme.out.print("\nBoard:\n");
 		for (int i = 0; i < state.getHeight(); i++)
 		{
 			for (int j = 0; j < state.getWidth(); j++)
 			{
 				saveme.out.print(state.getBoardCells()[i][j]);
 				saveme.out.print(' ');
 			}
 			saveme.out.print('\n');
 		}
 		//save transitions
 		saveme.out.print("Transitions:\n");
 		for (BoardState transist_state : state.getTransitionsFrom())
 			saveme.printTransitions(transist_state);
 		saveme.out.close();
 		return true;
 	}
 	public void printTransitions(BoardState state)
 	{
 		out.print("newState:\n");
 		out.println(state.getJustification());
 		out.println(state.getCaseRuleJustification());
 		//print offset
 		out.print(state.getOffset().x);
 		out.print(' ');
 		out.print(state.getOffset().y);
 		out.print('\n');
 		out.print(state.isModifiable());
 		out.print('\n');
 		
 		for(Point cell : state.getChangedCells())
 		{
 			out.print(cell.x);
 			out.print(' ');
 			out.print(cell.y);
 			out.print(' ');
 			out.print(state.getBoardCells()[cell.y][cell.x]);
 			out.print('\n');
 		}
 		//recurse through children
 		for (BoardState transition_state : state.getTransitionsFrom())
 			printTransitions(transition_state);
 				
 		out.print("endLeaf:\n");
 	}
 	public SaveableProofState[] getStates()
 	{
 		return states;
 	}
 
 	public void setStates(SaveableProofState[] states)
 	{
 		this.states = states;
 	}
 
 	public Vector<SaveableProofTransition> getTransitions()
 	{
 		return transitions;
 	}
 
 	public void setTransitions(Vector<SaveableProofTransition> transitions)
 	{
 		this.transitions = transitions;
 	}
 }
