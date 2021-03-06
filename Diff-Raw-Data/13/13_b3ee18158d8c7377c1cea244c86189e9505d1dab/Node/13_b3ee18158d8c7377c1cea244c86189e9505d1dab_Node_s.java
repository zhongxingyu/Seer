 package topiaryexplorer;
 
 /**
  * Represents a node in the tree, which may be a leaf or internal node.
  */
 
 import java.awt.*;
 import java.util.*;
 
 public class Node implements Cloneable{
   private Node parent = null;
   public ArrayList<Node> nodes = new ArrayList(); //children
   private ArrayList<Node> anscestors = new ArrayList();
 
   private String label = "";
   private String name = "";
   private String lineage = "";
   private String consensusLineage = null;
   private String tconsensusLineage = null;
   private double branchlength = 0;
   
   private double depth = 0;
   private int numberOfLeaves = 0;
   
   private double yoffset = 0;
   private double xoffset = 0;
   private double roffset = 0; //radius
   private double toffset = 0; //theta
   private double rxoffset = 0;
   private double ryoffset = 0;
   private boolean drawPie = false; //should a pie chart be drawn for this node?
   private boolean drawLabel = true;
   private boolean locked = false;
   private double maximumYOffset = 0;
   private double minimumYOffset = 0;
   private double maximumTOffset = 0;
   private double minimumTOffset = 0;
   private double maximumROffset = 0;
   private double minimumROffset = 0;
   private double maximumRXOffset = 0;
   private double minimumRXOffset = 0;
   
   //parallel arrays of colors and the weight to be drawn with each
   private boolean branchColored = true;
   HashMap branchColorMap = new HashMap();
   private Color branchColor = null;
   private ArrayList<Color> groupBranchColor = new ArrayList<Color>();
   private ArrayList<Double> groupBranchWeight = new ArrayList<Double>();
   
   private boolean labelColored = true;
   HashMap labelColorMap = new HashMap();
   private Color labelColor = null;
   private ArrayList<Color> groupLabelColor = new ArrayList<Color>();
   private ArrayList<Double> groupLabelWeight = new ArrayList<Double>();
   
   private double lineWidth = 1;
 
   private boolean collapsed = false; //if true, the children are not shown (draws a wedge)
   private boolean sliderCollapsed = true;
   private boolean hidden = false;
 
   Object userObject = null;
   String userString = "";
 
 
   Node() {}
 
   Node(String _label, double _branchlength) {
     label = _label;
     name = _label;
     branchlength = _branchlength;
   }
 
   //GETTERS AND SETTERS
   public boolean isHidden() { return hidden; }
   public void setHidden(boolean cond) { hidden = cond; }
   public boolean isLocked() { return locked; }
   public boolean isLeaf() { return nodes.size() == 0; }
   public void setDrawPie(boolean b) { drawPie = b; }
   public boolean getDrawPie() { return drawPie; }
   public void setDrawLabel(boolean b) { drawLabel = b; }
   public boolean getDrawLabel() { return drawLabel; }
   public ArrayList<Color> getGroupBranchColor() { return groupBranchColor; }
   public ArrayList<Double> getGroupBranchFraction() { return groupBranchWeight; }
   public ArrayList<Color> getGroupLabelColor() { return groupLabelColor; }
   public ArrayList<Double> getGroupLabelFraction() { return groupLabelWeight; }
   public boolean isCollapsed() { return collapsed || sliderCollapsed; }
   public void setSliderCollapsed(boolean cond) { if(!locked) sliderCollapsed = cond; }
   public void setCollapsed(boolean cond) { if(!locked && parent != null) collapsed = cond; }
   public void setLabel(String s) { label = s; }
   public String getLabel() { return label; }
   public double getBranchLength() { return branchlength; }
   public void setName(String s) { name = s; }
   public String getName() { return name; }
   public void setLineage(String s) { lineage = s; }
   public String getLineage() { 
       if(!lineage.equals("Unclassified-Screened"))
           return lineage;
        return ""; 
         }
   public void setConsensusLineage(String s) { consensusLineage = s; }
   public String getConsensusLineage() { return consensusLineage;}
   public void setBranchLength(double f) { if (f >= 0) branchlength = f; }
   public double getYOffset() { return yoffset; }
   public double getXOffset() { return xoffset; }
   public void setYOffset(double f) { yoffset = f; }
   public void setXOffset(double f) { xoffset = f; }
   public double getROffset() { return roffset; }
   public double getTOffset() { return toffset; }
   public void setROffset(double f) { roffset = f; }
   public void setTOffset(double f) { toffset = f; }  
   public double getRXOffset() { return rxoffset; }
   public double getRYOffset() { return ryoffset; }
   public void setRXOffset(double f) { rxoffset = f; }
   public void setRYOffset(double f) { ryoffset = f; }   
   public Node getParent() { return parent; }
   public double getMaximumYOffset() { return maximumYOffset; }
   public void setMaximumYOffset(double f) { maximumYOffset = f; }
   public double getMinimumYOffset() { return minimumYOffset; }
   public void setMinimumYOffset(double f) { minimumYOffset = f; }
   public double getMaximumTOffset() { return maximumTOffset; }
   public void setMaximumTOffset(double f) { maximumTOffset = f; }
   public double getMinimumTOffset() { return minimumTOffset; }
   public void setMinimumTOffset(double f) { minimumTOffset = f; }
   public double getMaximumROffset() { return maximumROffset; }
   public void setMaximumROffset(double f) { maximumROffset = f; }
   public double getMinimumROffset() { return minimumROffset; }
   public void setMinimumROffset(double f) { minimumROffset = f; }  
   public double getMinimumRXOffset() { return minimumRXOffset; }
   public void setMinimumRXOffset(double f) { minimumRXOffset = f; }    
   public double getMaximumRXOffset() { return maximumRXOffset; }
   public void setMaximumRXOffset(double f) { maximumRXOffset = f; }  
   public double getLineWidth() { return lineWidth; }
   public void setLineWidth(double f) { lineWidth = f; }
   public void setAnscestors(ArrayList<Node> a) { anscestors = a; }
   public ArrayList<Node> getAnscestors() { return anscestors; }
   public int getLevel() { return anscestors.size(); }
   public void setDepthO(double val) { depth = val;}
   public double depth() { return depth; }
   public void setNumberOfLeavesO(int val) { numberOfLeaves = val;}
   public int getNumberOfLeaves() { return numberOfLeaves; }
   
   
   public void setLocked(boolean l) { 
       locked=l; 
       for(Node n: nodes)
         n.setLocked(l);
       }
   
   // recursive method to return consensus lineage
   public String getConsensusLineageF() {   
       // If the node is a leaf return lineage
       if(isLeaf())
       {
           if(!lineage.equals("Unclassified-Screened"))
             return lineage;
          return "";
       }
       
       ArrayList<Node> tips = getLeaves();
       
       // Collect lineage of tips
       ArrayList<String> currLabels = new ArrayList<String>();
       for(Node n: tips)
       {
           String l = n.getLineage();
          currLabels.add(l);
       }
       
       String consensusLineage = "";
       ArrayList<String> curr = new ArrayList<String>();
       ArrayList<String> newLabels = new ArrayList<String>();
       // test if strings have at least one entry ie "bacteria;"
       String test = currLabels.get(0); 
       boolean loop = true;
       boolean testb = true;
       
       // while test string has one entry and continue looping
       while(test.indexOf(";") != -1 && loop)
       {
           curr = new ArrayList<String>();
           newLabels = new ArrayList<String>();
           testb = true;
           for(String l: currLabels)
           {                
               try {
               // add first entry to curr, keep rest of labels in newLabels
               String entry = l.substring(0,l.indexOf(";")).trim();
               curr.add(entry);        
             }
           catch(StringIndexOutOfBoundsException e)
             {
                 // if there is no ";"
                 curr.add(l.trim());
                 testb = false;
             }
             if(l.length() > 0)
                 newLabels.add(l.substring(l.indexOf(";")+1, l.length()).trim());      
           }
         
           String c = TopiaryFunctions.getConsensus(curr,.5);
           if(c != null)
           {
           consensusLineage += c + ";";
           currLabels = newLabels;
           }
           else
               break;
               
         test = currLabels.get(0);
       }
       return consensusLineage;
   }
   
   public int getNumberOfLeavesF() {
       int total = 0;
       if (isLeaf()) {
           total = 1;
       } else {
           for (Node n : nodes) {
               total = total + n.getNumberOfLeaves();
           }
       }
       numberOfLeaves = total;
       return total;
   }
 
   /**
    * Based on the groupBranchWeight and groupBranchColor field, return an overall blended color
    */
  public Color getBranchColor(boolean majority) {        
      //if there's no color, use black
      if (!branchColored) {
          return new Color(0,0,0);
      }
      double r,g,b;
      r = g = b = 0;
      
      if(majority)
      {
          double max = -100;
          Color majorityColor = new Color(0);
          if(groupBranchColor.size() == 1)
             return groupBranchColor.get(0);
 
          for(int i = 0; i < groupBranchColor.size(); i++)
          {
              if(groupBranchWeight.get(i) > max)
              {
                  majorityColor = groupBranchColor.get(i);
                  max = groupBranchWeight.get(i);
              }
          }
          branchColor = majorityColor;
      }
      else {
         double total = 0;
         for (Double weight : groupBranchWeight) {
           total = total + weight;
         }
         for (int i = 0; i < groupBranchWeight.size(); i++) {
           r += groupBranchWeight.get(i)/total*groupBranchColor.get(i).getRed();
           g += groupBranchWeight.get(i)/total*groupBranchColor.get(i).getGreen();
           b += groupBranchWeight.get(i)/total*groupBranchColor.get(i).getBlue();
         }
         branchColor = new Color(Math.abs((float)r/255),Math.abs((float)g/255),Math.abs((float)b/255));
     }
     return branchColor;
   }
   
   public void noBranchColor() {
     branchColored = false;
   }
 
   public void clearBranchColor() {
     branchColor = null;
     groupBranchWeight = new ArrayList<Double>();
     groupBranchColor = new ArrayList<Color>();
   }
 
   public void addBranchColor(Color c, double w) {
       branchColored = true;
       branchColorMap.put(c,w);
       groupBranchWeight.add(new Double(w));
       groupBranchColor.add(c);
   }
   
   public Color getLabelColor(boolean majority) {        
        //if there's no color, use black
        if (!labelColored) {
            return new Color(0,0,0);
        }
        double r,g,b;
        r = g = b = 0;
 
        if(majority)
        {
            double max = -100;
            Color majorityColor = new Color(0);
            if(groupLabelColor.size() == 0)
              return new Color(0,0,0);
            if(groupLabelColor.size() == 1)
              return groupLabelColor.get(0);
 
            for(int i = 0; i < groupLabelColor.size(); i++)
            {
                if(groupLabelWeight.get(i) > max)
                {
                    majorityColor = groupLabelColor.get(i);
                    max = groupLabelWeight.get(i);
                }
            }
            labelColor = majorityColor;
        }
        else {
           double total = 0;
           for (Double weight : groupLabelWeight) {
             total = total + weight;
           }
           for (int i = 0; i < groupBranchWeight.size(); i++) {
             r += groupLabelWeight.get(i)/total*groupLabelColor.get(i).getRed();
             g += groupLabelWeight.get(i)/total*groupLabelColor.get(i).getGreen();
             b += groupLabelWeight.get(i)/total*groupLabelColor.get(i).getBlue();
           }
           labelColor = new Color(Math.abs((float)r/255),Math.abs((float)g/255),Math.abs((float)b/255));
       }
       return labelColor;
     }
 
     public void noLabelColor() {
       labelColored = false;
     }
 
     public void clearLabelColor() {
       labelColor = null;
       groupLabelWeight = new ArrayList<Double>();
       groupLabelColor = new ArrayList<Color>();
     }
 
     public void addLabelColor(Color c, double w) {
         labelColored = true;
         labelColorMap.put(c,w);
         groupLabelWeight.add(new Double(w));
         groupLabelColor.add(c);
     }
   
   /**
    * Returns a list of all the leaves of the tree
    */
   public ArrayList<Node> getLeaves() {
       ArrayList<Node> result = new ArrayList<Node>();
 
       if (isLeaf()) {
           result.add(this);
           return result;
       }
       
       for (Node n : nodes) {
           result.addAll(n.getLeaves());
       }
       return result;
   }
   
 
   /**
    * Returns all of the nodes of the tree.
    */
   public ArrayList<Node> getNodes() {
       ArrayList<Node> result = new ArrayList<Node>();
       for (Node n: nodes) {
           result.addAll(n.getNodes());
       }
       result.add(this);
       return result;
   }
 
   /**
    * Add a child to the tree
    */
   public void addChild(Node child) {
     nodes.add(child);
     child.parent = this;
     child.setAnscestors(this.anscestors);
     child.getAnscestors().add(this);
     updateBranchColorFromChildren();
   }
 
   public void rotate() {
       Stack<Node> s = new Stack<Node>();
       for (Node n : nodes) {
           s.push(n);
       }
       nodes.clear();
       while (s.size() > 0) {
           nodes.add(s.pop());
       }
   }
 
   /**
    * Max depth of the tree (as a sum of branch lengths)
    */
   public double depthF() {
     double deepest = 0;
     for (int i = 0; i <nodes.size(); i++) {
       double depth = nodes.get(i).depth();
       if (depth > deepest) {
         deepest = depth;
       }
     }
     return deepest + getBranchLength();
   }
 
 
   /**
    * Returns the shortest root-to-tip branch length of the tree
    */
   public double shortestRootToTipDistance() {
     if (nodes.size() == 0) {
       return getBranchLength();
     }
     double shortest = 100000000;
     for (int i = 0; i < nodes.size(); i++) {
       double s = nodes.get(i).shortestRootToTipDistance();
       if (s < shortest) {
         shortest = s;
       }
     }
     return shortest + getBranchLength();
   }
 
   /**
    * Returns the longest root-to-tip branch length of the tree
    */
   public double longestRootToTipDistance() {
     if (nodes.size() == 0) {
       return getBranchLength();
     }
     double longest = -100000000;
     for (int i = 0; i < nodes.size(); i++) {
       double l = nodes.get(i).longestRootToTipDistance();
       if (l > longest) {
         longest = l;
       }
     }
     return longest + getBranchLength();
   }
 
   /**
    * Returns the longest label of any node in the tree
    */
   public String getLongestLabel() {
     String longest = getLabel();
     for (int i = 0; i < nodes.size(); i++) {
       String lbl = nodes.get(i).getLongestLabel();
       if (lbl.length() > longest.length()) {
         longest = lbl;
       }
     }
     return longest;
   }
 
 
   //SORTING METHODS
   public void sortByNumberOfOtus() {
     //sort each of the subtrees
     for (int i=0; i < nodes.size(); i++) {
       nodes.get(i).sortByNumberOfOtus();
     }
 
     //sort this node
     Collections.sort(nodes, new java.util.Comparator() {
       public int compare(Object o1, Object o2) {
         if ( ((Node)o1).getNumberOfLeaves() < ((Node)o2).getNumberOfLeaves()) {
           return -1;
         } else if ( ((Node)o1).getNumberOfLeaves() > ((Node)o2).getNumberOfLeaves()) {
           return 1;
         } else {
           return 0;
         }
       }
     });
   }
   
   public void sortByBranchLength() {
       for (int i=0; i < nodes.size(); i++) {
         nodes.get(i).sortByBranchLength();
       }
 
       //sort this node
       Collections.sort(nodes, new java.util.Comparator() {
         public int compare(Object o1, Object o2) {
           if ( ((Node)o1).longestRootToTipDistance() < ((Node)o2).longestRootToTipDistance()) {
             return -1;
           } else if ( ((Node)o1).longestRootToTipDistance() > ((Node)o2).longestRootToTipDistance()) {
             return 1;
           } else {
             return 0;
           }
         }
       });
   }
 
   public void sortByNumberOfChildren() {
         //sort each of the subtrees
     for (int i=0; i < nodes.size(); i++) {
       nodes.get(i).sortByNumberOfChildren();
     }
 
     //sort this node
     Collections.sort(nodes, new java.util.Comparator() {
       public int compare(Object o1, Object o2) {
         if ( ((Node)o1).nodes.size() < ((Node)o2).nodes.size()) {
           return -1;
         } else if ( ((Node)o1).nodes.size() > ((Node)o2).nodes.size()) {
           return 1;
         } else {
           return 0;
         }
       }
     });
   }
 
   /**
    * Set the color by blending the children's colors, weighted by the number of leaves in each child
    * this recursively works over the entire tree.
    */
   public void updateBranchColorFromChildren() {
     if (isLeaf()) { aggregateBranchData(); return; }
 
     //make the lists empty
     branchColor = null;
     groupBranchColor = new ArrayList<Color>();
     groupBranchWeight = new ArrayList<Double>();
     for (int i=0; i < nodes.size(); i++) {
       //recursion
       nodes.get(i).updateBranchColorFromChildren();
 
       //get the overall color for this node
       for (int j = 0; j < nodes.get(i).groupBranchColor.size(); j++) {
         groupBranchColor.add(nodes.get(i).groupBranchColor.get(j));
         groupBranchWeight.add(nodes.get(i).groupBranchWeight.get(j));
       }
     }
     aggregateBranchData();
   }
   
   public void updateLabelColorFromChildren() {
     if (isLeaf()) { aggregateLabelData(); return; }
 
     //make the lists empty
     labelColor = null;
     groupLabelColor = new ArrayList<Color>();
     groupLabelWeight = new ArrayList<Double>();
     for (int i=0; i < nodes.size(); i++) {
       //recursion
       nodes.get(i).updateLabelColorFromChildren();
 
       //get the overall color for this node
       for (int j = 0; j < nodes.get(i).groupLabelColor.size(); j++) {
         groupLabelColor.add(nodes.get(i).groupLabelColor.get(j));
         groupLabelWeight.add(nodes.get(i).groupLabelWeight.get(j));
       }
     }
     aggregateLabelData();
   }
   
   public double getLineWidthF() {
       if (isLeaf()) {
           return 1; 
           }
 
       //make the lists empty
       double total = 0;
       for (int i=0; i < nodes.size(); i++) {
         //recursion
         nodes.get(i).updateLineWidthsFromChildren();
 
         total = total + nodes.get(i).getLineWidth();
       }
       return total/nodes.size();
     }
   
   public void updateLineWidthsFromChildren() {
     if (isLeaf()) { return; }
 
     //make the lists empty
     double total = 0;
     for (int i=0; i < nodes.size(); i++) {
       //recursion
       nodes.get(i).updateLineWidthsFromChildren();
 
       total = total + nodes.get(i).getLineWidth();
     }
     setLineWidth(total/nodes.size());
   }
 
   /**
    * Put all of the same colors together 
    */
   public void aggregateBranchData() {
     ArrayList<Color> newgroupBranchColor = new ArrayList<Color>();
     ArrayList<Double> newgroupBranchWeight = new ArrayList<Double>();
 
     for (int i = 0; i < groupBranchColor.size(); i++) {
       if (newgroupBranchColor.contains(groupBranchColor.get(i))) {
           int index = newgroupBranchColor.indexOf(groupBranchColor.get(i));
           newgroupBranchWeight.set(index, newgroupBranchWeight.get(index) + groupBranchWeight.get(i));
       } else {
         newgroupBranchColor.add(groupBranchColor.get(i));
         newgroupBranchWeight.add(groupBranchWeight.get(i));
       }
     }
 
     groupBranchWeight = newgroupBranchWeight;
     groupBranchColor = newgroupBranchColor;
   }
   
   public void aggregateLabelData() {
         ArrayList<Color> newgroupLabelColor = new ArrayList<Color>();
         ArrayList<Double> newgroupLabelWeight = new ArrayList<Double>();
 
         for (int i = 0; i < groupLabelColor.size(); i++) {
           if (newgroupLabelColor.contains(groupLabelColor.get(i))) {
               int index = newgroupLabelColor.indexOf(groupLabelColor.get(i));
               newgroupLabelWeight.set(index, newgroupLabelWeight.get(index) + groupLabelWeight.get(i));
           } else {
             newgroupLabelColor.add(groupLabelColor.get(i));
             newgroupLabelWeight.add(groupLabelWeight.get(i));
           }
         }
 
         groupLabelWeight = newgroupLabelWeight;
         groupLabelColor = newgroupLabelColor;
      }
 }
