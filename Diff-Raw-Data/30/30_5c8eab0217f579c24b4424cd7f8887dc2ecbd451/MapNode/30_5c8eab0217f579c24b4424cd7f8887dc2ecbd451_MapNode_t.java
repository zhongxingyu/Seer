 package org.glowacki.core.dungen;
 
 import org.glowacki.core.astar.INode;
 
 public class MapNode
     implements Comparable, INode
 {
     enum EndPoint
     {
         START, END, NONE;
     }
 
     private int x;
     private int y;
     private EndPoint endPt;
     private RoomType type;
     private INode parent;
 
      // cost of getting from parent node to this node
     private double parentCost = Double.MIN_VALUE;
 
     // last node used to compute passthrough cost
     private INode lastNode;
     // cost of getting from the start to the goal through this node
     private double passThroughCost = Double.MIN_VALUE;
 
     MapNode(int x, int y)
     {
         this(x, y, EndPoint.NONE);
     }
 
     MapNode(int x, int y, EndPoint endPt)
     {
         this.x = x;
         this.y = y;
         this.endPt = endPt;
         this.type = RoomType.EMPTY;
     }
 
     public void clear()
     {
         endPt = EndPoint.NONE;
 
         parent = null;
         parentCost = Double.MIN_VALUE;
 
         lastNode = null;
         passThroughCost = Double.MIN_VALUE;
     }
 
     public int compareTo(Object obj)
     {
         if (obj == null) {
             return -1;
         }
 
         if (!(obj instanceof MapNode)) {
             return getClass().getName().compareTo(obj.getClass().getName());
         }
 
         MapNode n = (MapNode) obj;
 
         int val = x - n.x;
         if (val == 0) {
             val = y - n.y;
         }
 
         return val;
     }
 
     public boolean equals(Object obj)
     {
         return compareTo(obj) == 0;
     }
 
     public char getChar()
     {
 if (hackChar != (char) 0) return hackChar;
         return type.getChar();
     }
 
 private char hackChar;
     void hackChar(char ch)
     {
         hackChar = ch;
     }
 
     public INode getParent()
     {
         return parent;
     }
 
     public double getParentCost()
     {
         if (isStart()) {
             return 0.0;
         } else if (parent == null) {
             return 999.0;
         }
 
         if (Double.compare(parentCost, Double.MIN_VALUE) == 0) {
             parentCost = 1.0 + .5 * (parent.getParentCost() - 1.0);
         }
 
         return parentCost;
     }
 
     public double getPassThrough(INode goal)
     {
         if (isStart()) {
             return 0.0;
         }
 
         if (Double.compare(passThroughCost, Double.MIN_VALUE) == 0 ||
             lastNode == null || lastNode.getX() != goal.getX() ||
             lastNode.getY() != goal.getY())
         {
             double localCost = 1.0 * (Math.abs(x - goal.getX()) +
                                       Math.abs(y - goal.getY()));
 
             double extraCost;
             if (isMovable()) {
                 extraCost = 0.0;
             } else if (type == RoomType.EMPTY) {
                 extraCost = 5.0;
             } else if (isWall()) {
                MapNode p = (MapNode) parent;
                if (p != null && (p.isWall() || p.isDoor())) {
                    extraCost = 20.0;
                } else {
                    extraCost = 5.5;
                }
             } else {
                 throw new Error("Not handling " + type);
             }
 
             passThroughCost = localCost + extraCost + getParentCost();
             lastNode = goal;
         }
 
         return passThroughCost;
     }
 
    public RoomType getType()
    {
        return type;
    }

     public int getX()
     {
         return x;
     }
 
     public int getY()
     {
         return y;
     }
 
     public int hashCode()
     {
         return (x & 0xffff) << 16 | (y & 0xffff);
     }
 
    public boolean isDoor()
    {
        return type == RoomType.DOOR;
    }

     public boolean isEmpty()
     {
         return type == RoomType.EMPTY;
     }
 
     public boolean isEnd()
     {
         return endPt == EndPoint.END;
     }
 
     public boolean isMovable()
     {
         return type == RoomType.FLOOR || type == RoomType.DOOR ||
             type == RoomType.TUNNEL;
     }
 
     public boolean isStart()
     {
         return endPt == EndPoint.START;
     }
 
     public boolean isWall()
     {
         return type == RoomType.WALL;// || type == RoomType.SIDEWALL;
     }
 
     void setEndPoint(EndPoint pt)
     {
         endPt = pt;
     }
 
     public void setParent(INode parent)
     {
         INode grandparent = parent.getParent();
         if (grandparent != null && grandparent.getX() == x &&
             grandparent.getY() == y)
         {
             final String pStr;
             if (this.parent == null) {
                 pStr = "";
             } else {
                 pStr = String.format(" (parent is %d,%d)",
                                      this.parent.getX(), this.parent.getY());
             }
 
             System.out.format("Cannot set %d,%d parent to child %d,%d%s", x, y,
                               parent.getX(), parent.getY(), pStr);
         } else {
 System.out.format("%d,%d parent is %d,%d\n", x, y, parent.getX(), parent.getY());
             this.parent = parent;
             parentCost = Double.MIN_VALUE;
 
             lastNode = null;
             passThroughCost = Double.MIN_VALUE;
         }
     }
 
     public void setType(RoomType type)
     {
         this.type = type;
     }
 
     public String toString()
     {
         return String.format("%d,%d:%s%s", x, y, type,
                              (endPt == EndPoint.START ? "start" :
                               (endPt == EndPoint.END ? "end" : "")));
     }
 }
