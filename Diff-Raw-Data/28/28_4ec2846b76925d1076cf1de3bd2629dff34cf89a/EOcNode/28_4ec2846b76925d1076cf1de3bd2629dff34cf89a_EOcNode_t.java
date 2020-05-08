 /*
  *EERT = EERT enhanced rendering technology
  *
  *Copyright (C) [2008] [Robert "BuRnEr" Schadek]
 
  *This program is free software; you can redistribute it and/or modify it under 
  *the terms of the GNU General Public License as published by the Free Software
  *Foundation; either version 3 of the License, 
  *or (at your option) any later version.
 
  *This program is distributed in the hope that it will be useful, but WITHOUT 
  *ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
  *FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  *You should have received a copy of the GNU General Public License along with 
  *this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package Engine;
 
 import Types.Obj;
 import Types.ObjIns;
 import Types.Vector;
 import java.util.LinkedList;
 import javax.media.opengl.GL;
 
 public class EOcNode {
 
     private EOcMaster root;
     public EOcNode[] childs;
     public ObjIns[] objs;
     private boolean[] drawn;
     private float xSize,  ySize,  zSize;
     private Vector middle;
     private float radius;
     private Obj[] realObjs;
     private int depth;
 
     public EOcNode(EOcMaster root, Obj[] realObjs, ObjIns[] objs, Vector middle, float xSize, float ySize, float zSize, boolean[] drawn, int depth) {
         this.root = root;
         this.realObjs = realObjs;
 
         this.middle = middle;
         this.depth = depth;
 
         this.drawn = drawn;
         this.xSize = xSize;
         this.ySize = ySize;
         this.zSize = zSize;
 
         float xSizeH = xSize / 2;
         float ySizeH = ySize / 2;
         float zSizeH = zSize / 2;
 
         this.radius = (float) Math.abs(Math.sqrt(Math.pow(xSizeH, 2) + Math.pow(ySizeH, 2) + Math.pow(zSizeH, 2)));
         this.objs = checkAllObjects(objs);
 
        if (this.depth < 4) {
            this.childs = makeChilds(xSizeH, ySizeH, zSizeH);
        }
     }
 
     private void eOcNodeInfo() {
         System.out.println("Middle x = " + this.middle.x + " Middle y = " + this.middle.y + " Middle z = " + this.middle.z);
         System.out.println("Depth = " + depth + " x = " + this.xSize / 2 + " y = " + this.ySize / 2 + " z = " + this.zSize / 2);
     }
 
    public void draw(GL gl) {
         if (this.childs == null) {
             float dis;
             if (0.0f == (dis = SphereInFrustum(this.middle.x, this.middle.y, this.middle.z, this.radius))) {
                 return;
             } else {
                 for (ObjIns obIns : this.objs) {
                    if (!this.root.drawn[obIns.objInsNumber]) {
                        //System.out.println("ObjIns = " + obIns.objInsNumber);
                         obIns.parent.render(obIns.objInsNumber, dis);
                         this.root.drawn[obIns.objInsNumber] = true;
                    } else {
                         continue;
                     }
                 }
             }
         } else {
             drawBox(gl);
             for (EOcNode child : this.childs) {
                 child.draw(gl);
             }
             return;
         }
     }
 
     private float SphereInFrustum(float x, float y, float z, float radius) {
         int p;
         float d = 0.0f;
 
         for (p = 0; p < 6; p++) {
             d = this.root.frustum[p][0] * x + this.root.frustum[p][1] * y + this.root.frustum[p][2] * z + this.root.frustum[p][3];
             if (d <= -radius) {
                 return 0;
             }
         }
         return d + radius;
     }
 
     private ObjIns[] checkAllObjects(ObjIns[] objs) {
         LinkedList<ObjIns> objects = new LinkedList<ObjIns>();
         for (ObjIns obj : objs) {
             if (checkForObj(obj)) {
                 objects.add(obj);
             }
         }
         ObjIns[] ret = new ObjIns[objects.size()];
         return ret = objects.toArray(ret);
     }
 
     private boolean checkForObj(ObjIns obj) {
         //Cheated a bit 
         //Just check a sphear around the box against the sphear around the Obj
         //the error-margin shouldn't be to big, but the check is much cheaper
         //and easier to understand
 
         float boxSph = (float) Math.sqrt(Math.pow((this.xSize / 2), 2) + Math.pow((this.ySize / 2), 2) + Math.pow((this.zSize / 2), 2));
         //Euclidean distance
         float dis = (float) Math.abs(Math.sqrt(Math.pow(this.middle.x - obj.origin.x, 2) + Math.pow(this.middle.y - obj.origin.y, 2) + Math.pow(this.middle.z - obj.origin.y, 2)));
 
         if (dis - this.radius - obj.boundSph < 0.0f) {
             return true;
         } else {
             return false;
         }
     }
 
     private void drawBox(GL gl) {
         gl.glPushMatrix();
         gl.glTranslatef(this.middle.x, this.middle.y, this.middle.z);
         gl.glColor3f(0.4f, 0.2f, 0.7f);
 
         //Back
         gl.glBegin(GL.GL_LINE_LOOP);
         gl.glVertex3f(-this.xSize / 2, -this.ySize / 2, -this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, -this.ySize / 2, -this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, this.ySize / 2, -this.zSize / 2);
         gl.glVertex3f(-this.xSize / 2, this.ySize / 2, -this.zSize / 2);
         gl.glEnd();
 
         //Front
         gl.glBegin(GL.GL_LINE_LOOP);
         gl.glVertex3f(-this.xSize / 2, -this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, -this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(-this.xSize / 2, this.ySize / 2, this.zSize / 2);
         gl.glEnd();
 
         //Left
         gl.glBegin(GL.GL_LINE_LOOP);
         gl.glVertex3f(-this.xSize / 2, -this.ySize / 2, -this.zSize / 2);
         gl.glVertex3f(-this.xSize / 2, -this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(-this.xSize / 2, this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(-this.xSize / 2, this.ySize / 2, -this.zSize / 2);
         gl.glEnd();
 
         //Right
         gl.glBegin(GL.GL_LINE_LOOP);
         gl.glVertex3f(this.xSize / 2, -this.ySize / 2, -this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, -this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, this.ySize / 2, this.zSize / 2);
         gl.glVertex3f(this.xSize / 2, this.ySize / 2, -this.zSize / 2);
         gl.glEnd();
 
         gl.glPopMatrix();
     }
 
     private EOcNode[] makeChilds(float xSizeH, float ySizeH, float zSizeH) {
         LinkedList<EOcNode> tmpChilds = new LinkedList<EOcNode>();
         //important needs to be created within this contidional execution
         //otherwise the child test in draw does not work
         EOcNode ch1 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x - xSizeH / 2, middle.y - ySizeH / 2, middle.z - zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch1.objs.length > 0) {
             tmpChilds.add(ch1);
         }
 
         EOcNode ch2 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x - xSizeH / 2, middle.y - ySizeH / 2, middle.z + zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch2.objs.length > 0) {
             tmpChilds.add(ch2);
         }
 
         EOcNode ch3 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x + xSizeH / 2, middle.y - ySizeH / 2, middle.z - zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch3.objs.length > 0) {
             tmpChilds.add(ch3);
         }
 
         EOcNode ch4 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x + xSizeH / 2, middle.y - ySizeH / 2, middle.z + zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch4.objs.length > 0) {
             tmpChilds.add(ch4);
         }
        EOcNode ch5 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x - xSizeH / 2, middle.y + ySizeH / 2, middle.z - zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch5.objs.length > 0) {
             tmpChilds.add(ch5);
         }
 
         EOcNode ch6 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x - xSizeH / 2, middle.y + ySizeH / 2, middle.z + zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch6.objs.length > 0) {
             tmpChilds.add(ch6);
         }
 
         EOcNode ch7 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x + xSizeH / 2, middle.y + ySizeH / 2, middle.z - zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch7.objs.length > 0) {
             tmpChilds.add(ch7);
         }
 
         EOcNode ch8 = new EOcNode(this.root, this.realObjs, this.objs, new Vector(middle.x + xSizeH / 2, middle.y + ySizeH / 2, middle.z + zSizeH / 2), xSizeH, ySizeH, zSizeH, this.drawn, depth + 1);
         if (ch8.objs.length > 0) {
             tmpChilds.add(ch8);
         }
 
         if (tmpChilds.size() == 0) {
             return null;
         } else {
             EOcNode[] retChilds = new EOcNode[tmpChilds.size()];
             return retChilds = tmpChilds.toArray(retChilds);
         }
     }
 }
