 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class BlockDisplay extends JPanel implements ActionListener{
     
     final Border noBorder = BorderFactory.createEmptyBorder();
     final Color maroon = new Color(170, 0, 0);
     boolean infoKnown = false;
     private int downX, upX;
     private long usedSpace;
     private long spacerScale;
     private boolean[] selected;
     private JButton[] markerButtons;
     private JLabel[] spaceLabels;
     private Vector blockLabels = new Vector();
     private Vector markers, blocks;
     private DPrimePanel theDPrime;
     GridBagLayout gridbag = new GridBagLayout();
     GridBagConstraints c = new GridBagConstraints();
 
     BlockDisplay(Vector ms, Vector bs, DPrimePanel dp, boolean ik){
 	markers = ms;
 	blocks = bs;
 	theDPrime = dp;
 	infoKnown = ik;
 
 	setBackground(Color.black);
 	setLayout(gridbag);
 
 	selected = new boolean[markers.size()];
 	markerButtons = new JButton[markers.size()];
 	spaceLabels = new JLabel[markers.size() - 1];
 	
 	//create a button to represent each marker
 	for (int i = 0; i < markers.size(); i++){
 	    markerButtons[i] = new JButton(String.valueOf(i+1));
 	    markerButtons[i].setBackground(Color.white);
 	    markerButtons[i].setForeground(Color.black);
 	    markerButtons[i].setPreferredSize(new Dimension(20,30));
 	    markerButtons[i].setBorder(noBorder);
 	    selected[i] = false;
 	    markerButtons[i].addActionListener(this);
 	}
 	//scale the spacing to fit on the screen
	spacerScale = (((SNP)markers.lastElement()).getPosition() - ((SNP)markers.firstElement()).getPosition())/(779-(20*markers.size()));
 	//System.out.println(spacerScale);
 	//make sure markers are at least slightly separated (no more than 300 bp/pixel)
 	//above formula generates a negative number for large datasets, hence:
 	if (spacerScale < 0) spacerScale=300;
 	//also don't want to spread them too far apart:
 	if (spacerScale < 100) spacerScale = 100;
 	//compute the total horizontal space used to display the markers
 	usedSpace = (20 * markers.size()) + (((SNP)markers.lastElement()).getPosition() - ((SNP)markers.firstElement()).getPosition())/spacerScale;
 	//crate a label for each space between markers
 	for (int i = 0; i < (markers.size() - 1); i++){
 	    spaceLabels[i] = new JLabel();
 	    //length is the distance between the ith marker and the ith+1
 	    long length = ((SNP)markers.elementAt(i+1)).getPosition() - ((SNP)markers.elementAt(i)).getPosition();
 	    if (infoKnown) spaceLabels[i].setToolTipText(((int) length/1000) + "kb");
 	    spaceLabels[i].setPreferredSize(new Dimension((int)(length/spacerScale), 4));
 	    spaceLabels[i].setOpaque(true);
 	    spaceLabels[i].setBackground(Color.white);
 	    spaceLabels[i].setBorder(noBorder);
 	}
 	//draw the markers
 	for (int i = 0; i < (markers.size() - 1); i++){
 	    add(markerButtons[i]);
 	    add(spaceLabels[i]);
 	}
 	//this is done after the loop because there is no spacer to be drawn after the
 	//last marker
 	add(markerButtons[(markers.size() - 1)]);
 
 	//now set up the initial haplotype blocks
 	for (int i = 0; i < blocks.size(); i++){
 	    int[] theBlock = (int[])blocks.elementAt(i);
 	    blockLabels.add(new JLabel());
 	    drawBlock(theBlock, i);
 	}
 	addMouseListener(new ClickAndDrag());
     }
 
     public void actionPerformed(ActionEvent e) {
 	//the -1 is because the index of the button is one less than its label
 	int theMarker = Integer.parseInt(e.getActionCommand()) - 1;
 	if (selected[theMarker]){
 	    //unhighlight it
 	    markerButtons[theMarker].setBackground(Color.white);
 	    markerButtons[theMarker].setForeground(Color.black);
 	    selected[theMarker] = false;
 	    int[] helper = {theMarker};
 	    theDPrime.hideBlock(helper);
 	    //remove it from its block
 	    for (int i = 0; i < blocks.size(); i++){
 		int[] theBlock = (int[])blocks.elementAt(i);
 		int[] fixedBlock = new int[theBlock.length - 1];
 		int found = -1;
 		for (int j = 0; j < theBlock.length; j++){
 		    if (theBlock[j] == theMarker){
 			found = j;
 		    }
 		}
 
 		//once the block to be edited is located save a new block which does not have the
 		//marker to be removed and delete the old block
 		
 		if (found != -1){		    
 		    if (theBlock.length == 2){
 			//simply remove a block if there's only one marker left in it
 			deleteBlock(i);
 		    }else{
 			int[] newArray = new int[theBlock.length - 1];
 			int l = 0;
 			for (int k = 0; k < theBlock.length; k++){
 			    if ( k != found){
 				newArray[l] = theBlock[k];
 				l++;
 			    }
 			}
 
 			deleteBlock(i);
 			blocks.insertElementAt(newArray, i);
 			blockLabels.insertElementAt(new JLabel(), i);
 			drawBlock(newArray, i);
 		    }
 		    
 		    //correct block numbers
 		    for (int q = 0; q < blocks.size(); q++){
 			remove((Component)blockLabels.elementAt(q));
 			drawBlock((int[])blocks.elementAt(q), q);
 		    }
 
 		    break;
 		}
 	    }
 	}
     }
 
     class ClickAndDrag extends MouseAdapter {
 	
 	public void mousePressed(MouseEvent e){
 	    downX = e.getX();
 	}
 	
 	public void mouseReleased(MouseEvent e){
 	    upX = e.getX();
 	    //if user drags right to left, fix it.
 	    if (upX < downX){
 		int temp = upX;
 		upX = downX;
 		downX = temp;
 	    }
 
 	    if ((upX - downX) > 20){
 		//which markers were selected?
 		Vector newBlock = new Vector();
 		for (int i = 0; i < markers.size(); i++){
 		    //computes what pixels this marker starts and stops at
 		    long screenBegin = ((((SNP)markers.elementAt(i)).getPosition() - ((SNP)markers.firstElement()).getPosition())/spacerScale) + (20 * i) + (getWidth() - usedSpace)/2;
 		    long screenEnd = screenBegin + 20;
 		    if (screenEnd > downX && screenBegin < upX){
 			newBlock.add(new Integer(i));
 		    }
 		}
 		if (newBlock.size() > 1){
 		    int[] newArray = new int[newBlock.size()];
 		    for (int i = 0; i < newBlock.size(); i++){
 			newArray[i] = ((Integer)newBlock.elementAt(i)).intValue();
 		    }
 		    
 		    for (int i = 0; i < blocks.size(); i++){
 			//if condition tests to see if block i overlaps anywhere with the
 			//new block
 			int firstnew = newArray[0]; int lastnew = newArray[newArray.length -1];
 			int firstold = ((int[])blocks.elementAt(i))[0];
 			int lastold = ((int[])blocks.elementAt(i))[((int[])blocks.elementAt(i)).length -1];
 			if ((firstnew <= lastold && firstnew >= firstold) || (lastnew >= firstold && firstnew <= firstold)){
 			    deleteBlock(i);
 			    i--;
 			}
 		    }
 
 		    boolean placed = false;
 		    for (int i = 0; i < blocks.size(); i++){
 			//insert this new block in the right place.
 			if (((int[])blocks.elementAt(i))[0] > newArray[0]){
 			    blocks.insertElementAt(newArray, i);
 			    blockLabels.insertElementAt(new JLabel(), i);
 			    drawBlock(newArray, i);
 			    placed = true;
 			    break;
 			}
 		    }
 		    if (!placed){
 			blocks.add(newArray);
 			blockLabels.add(new JLabel());
 		    }
 
 		    //correct block numbers
 		    for (int i = 0; i < blocks.size(); i++){
 			remove((Component)blockLabels.elementAt(i));
 			drawBlock((int[])blocks.elementAt(i), i);
 		    }
 		}
 	    }
 	}
     }
     
     void deleteBlock(int which){
 	int[] theBlock = (int[])blocks.elementAt(which);
 	for (int j = 0; j < theBlock.length - 1; j++){
 	    markerButtons[theBlock[j]].setBackground(Color.white);
 	    markerButtons[theBlock[j]].setForeground(Color.black);
 	    selected[theBlock[j]] = false;
 	}
 	markerButtons[theBlock[theBlock.length - 1]].setBackground(Color.white);
 	markerButtons[theBlock[theBlock.length - 1]].setForeground(Color.black);
 	selected[theBlock[theBlock.length - 1]] = false;
 	blocks.removeElementAt(which);
 	remove((Component)blockLabels.elementAt(which));
 	blockLabels.removeElementAt(which);
 	theDPrime.hideBlock(theBlock);
 	repaint();
     }	
     
     public void clearBlocks(){
 	//keep popping off the first block until none are left
 	int numBlocks = blocks.size();
 	for (int i = 0; i < numBlocks; i++){
 	    deleteBlock(0);
 	}
     }
 
     public Vector getBlocks(){
 	return blocks;
     }
 
     void drawBlock(int[] theBlock, int index){
 	if (infoKnown){
 	    int length = (int) (((SNP)markers.elementAt(theBlock[theBlock.length - 1])).getPosition() - ((SNP)markers.elementAt(theBlock[0])).getPosition())/1000;
 	    ((JLabel)blockLabels.elementAt(index)).setText("<html><font color='white'>Block " + (index+1) + "<p>(" + length + " kb)</font></html>");
 	}else{
 	    ((JLabel)blockLabels.elementAt(index)).setText("Block " + (index+1));
 	}
 	c.gridy=1;
 	JLabel blockLabel = ((JLabel)blockLabels.elementAt(index));
 	blockLabel.setOpaque(true);
 	blockLabel.setBackground(maroon);
 	blockLabel.setForeground(Color.white);
 	for (int j = 0; j < theBlock.length - 1 ; j++){
 	    markerButtons[theBlock[j]].setBackground(maroon);
 	    markerButtons[theBlock[j]].setForeground(Color.white);
 	    selected[theBlock[j]] = true;
 	}
 	markerButtons[theBlock[theBlock.length - 1]].setBackground(maroon);
 	markerButtons[theBlock[theBlock.length - 1]].setForeground(Color.white);
 	selected[theBlock[theBlock.length - 1]] = true;	
 	c.gridx = 2 * theBlock[0];
 	c.fill = GridBagConstraints.BOTH;
 	c.gridwidth = 2 * (theBlock[theBlock.length - 1] - theBlock[0]) + 1;
 	gridbag.setConstraints(blockLabel, c);
 	add(blockLabel);
 	theDPrime.showBlock(theBlock);
 	doLayout();
 	repaint();
     }
 }
