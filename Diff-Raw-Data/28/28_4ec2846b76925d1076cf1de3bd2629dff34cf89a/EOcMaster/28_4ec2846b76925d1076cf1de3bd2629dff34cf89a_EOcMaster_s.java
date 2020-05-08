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
 import javax.media.opengl.GL;
 
 public class EOcMaster {
 
     public float[][] frustum = new float[6][4];
     private ObjIns[] objs;
     private Obj[] realObjs;
     public boolean[] drawn;
     private EOcNode root;
     private Vector middle;
     private float xSize = 0;
     private float ySize = 0;
     private float zSize = 0;
     private GL gl;
 
     public EOcMaster(ObjIns[] allObj, GL gl, Obj[] objs) {
         this.drawn = new boolean[allObj.length];
         this.realObjs = objs;
         this.gl = gl;
         this.objs = allObj;
         makeFirstCubeInfo();
         this.root = new EOcNode(this, this.realObjs, this.objs, this.middle, this.xSize, this.ySize, this.zSize, this.drawn, 0);
     }
 
     private void makeFirstCubeInfo() {
         this.middle = new Vector();
         float xP = 0.0f;
         float yP = 0.0f;
         float zP = 0.0f;
         float xN = 0.0f;
         float yN = 0.0f;
         float zN = 0.0f;
         for (ObjIns obj : this.objs) {
             //middle
             this.middle.x += obj.origin.x / this.objs.length;
             this.middle.y += obj.origin.y / this.objs.length;
             this.middle.z += obj.origin.z / this.objs.length;
 
             //expension
             if (xN > obj.origin.x - obj.boundSph) {
                 xN = obj.origin.x - obj.boundSph;
             }
             if (yN > obj.origin.y - obj.boundSph) {
                 yN = obj.origin.y - obj.boundSph;
             }
             if (zN > obj.origin.z - obj.boundSph) {
                 zN = obj.origin.z - obj.boundSph;
             }
             if (xP < obj.origin.x + obj.boundSph) {
                 xP = obj.origin.x + obj.boundSph;
             }
             if (yP < obj.origin.y + obj.boundSph) {
                 yP = obj.origin.y + obj.boundSph;
             }
             if (zP < obj.origin.z + obj.boundSph) {
                 zP = obj.origin.z + obj.boundSph;
             }
         }
 
         xP = Math.abs(xP);
         yP = Math.abs(yP);
         zP = Math.abs(zP);
 
         xN = Math.abs(xN);
         yN = Math.abs(yN);
         zN = Math.abs(zN);
 
         if (xP >= xN) {
             this.xSize = xP * 2.0f;
         } else {
             this.xSize = xN * 2.0f;
         }
 
         if (xP >= xN) {
             this.ySize = yP * 2.0f;
         } else {
             this.ySize = yN * 2.0f;
         }
 
         if (zP >= zN) {
             this.zSize = zP * 2.0f;
         } else {
             this.zSize = zN * 2.0f;
         }
     }
 
     public void drawOctree(GL gl) {
         this.drawn = new boolean[this.objs.length];
         //drawBox(gl);
         extractFrustum();
         this.root.draw(gl);
     }
 
     public void drawBox(GL gl) {
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
 
     private void extractFrustum() {
         float[] proj = new float[16];
         float[] modl = new float[16];
         float[] clip = new float[16];
         float t;
 
         /* Get the current PROJECTION matrix from OpenGL */
         gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, proj, 0);
 
         /* Get the current MODELVIEW matrix from OpenGL */
         gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modl, 0);
 
         /* Combine the two matrices (multiply projection by modelview) */
         clip[ 0] = modl[ 0] * proj[ 0] + modl[ 1] * proj[ 4] + modl[ 2] * proj[ 8] + modl[ 3] * proj[12];
         clip[ 1] = modl[ 0] * proj[ 1] + modl[ 1] * proj[ 5] + modl[ 2] * proj[ 9] + modl[ 3] * proj[13];
         clip[ 2] = modl[ 0] * proj[ 2] + modl[ 1] * proj[ 6] + modl[ 2] * proj[10] + modl[ 3] * proj[14];
         clip[ 3] = modl[ 0] * proj[ 3] + modl[ 1] * proj[ 7] + modl[ 2] * proj[11] + modl[ 3] * proj[15];
 
         clip[ 4] = modl[ 4] * proj[ 0] + modl[ 5] * proj[ 4] + modl[ 6] * proj[ 8] + modl[ 7] * proj[12];
         clip[ 5] = modl[ 4] * proj[ 1] + modl[ 5] * proj[ 5] + modl[ 6] * proj[ 9] + modl[ 7] * proj[13];
         clip[ 6] = modl[ 4] * proj[ 2] + modl[ 5] * proj[ 6] + modl[ 6] * proj[10] + modl[ 7] * proj[14];
         clip[ 7] = modl[ 4] * proj[ 3] + modl[ 5] * proj[ 7] + modl[ 6] * proj[11] + modl[ 7] * proj[15];
 
         clip[ 8] = modl[ 8] * proj[ 0] + modl[ 9] * proj[ 4] + modl[10] * proj[ 8] + modl[11] * proj[12];
         clip[ 9] = modl[ 8] * proj[ 1] + modl[ 9] * proj[ 5] + modl[10] * proj[ 9] + modl[11] * proj[13];
         clip[10] = modl[ 8] * proj[ 2] + modl[ 9] * proj[ 6] + modl[10] * proj[10] + modl[11] * proj[14];
         clip[11] = modl[ 8] * proj[ 3] + modl[ 9] * proj[ 7] + modl[10] * proj[11] + modl[11] * proj[15];
 
         clip[12] = modl[12] * proj[ 0] + modl[13] * proj[ 4] + modl[14] * proj[ 8] + modl[15] * proj[12];
         clip[13] = modl[12] * proj[ 1] + modl[13] * proj[ 5] + modl[14] * proj[ 9] + modl[15] * proj[13];
         clip[14] = modl[12] * proj[ 2] + modl[13] * proj[ 6] + modl[14] * proj[10] + modl[15] * proj[14];
         clip[15] = modl[12] * proj[ 3] + modl[13] * proj[ 7] + modl[14] * proj[11] + modl[15] * proj[15];
         /* Extract the numbers for the RIGHT plane */
         this.frustum[0][0] = clip[ 3] - clip[ 0];
         this.frustum[0][1] = clip[ 7] - clip[ 4];
         this.frustum[0][2] = clip[11] - clip[ 8];
         this.frustum[0][3] = clip[15] - clip[12];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
         this.frustum[0][0] /= t;
         this.frustum[0][1] /= t;
         this.frustum[0][2] /= t;
         this.frustum[0][3] /= t;
 
         /* Extract the numbers for the LEFT plane */
         this.frustum[1][0] = clip[ 3] + clip[ 0];
         this.frustum[1][1] = clip[ 7] + clip[ 4];
         this.frustum[1][2] = clip[11] + clip[ 8];
         this.frustum[1][3] = clip[15] + clip[12];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
         this.frustum[1][0] /= t;
         this.frustum[1][1] /= t;
         this.frustum[1][2] /= t;
         this.frustum[1][3] /= t;
 
         /* Extract the BOTTOM plane */
         this.frustum[2][0] = clip[ 3] + clip[ 1];
         this.frustum[2][1] = clip[ 7] + clip[ 5];
         this.frustum[2][2] = clip[11] + clip[ 9];
         this.frustum[2][3] = clip[15] + clip[13];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
         this.frustum[2][0] /= t;
         this.frustum[2][1] /= t;
         this.frustum[2][2] /= t;
         this.frustum[2][3] /= t;
 
         /* Extract the TOP plane */
         this.frustum[3][0] = clip[ 3] - clip[ 1];
         this.frustum[3][1] = clip[ 7] - clip[ 5];
         this.frustum[3][2] = clip[11] - clip[ 9];
         this.frustum[3][3] = clip[15] - clip[13];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
         this.frustum[3][0] /= t;
         this.frustum[3][1] /= t;
         this.frustum[3][2] /= t;
         this.frustum[3][3] /= t;
         /* Extract the FAR plane */
         this.frustum[4][0] = clip[ 3] - clip[ 2];
         this.frustum[4][1] = clip[ 7] - clip[ 6];
         this.frustum[4][2] = clip[11] - clip[10];
         this.frustum[4][3] = clip[15] - clip[14];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
         this.frustum[4][0] /= t;
         this.frustum[4][1] /= t;
         this.frustum[4][2] /= t;
         this.frustum[4][3] /= t;
 
         /* Extract the NEAR plane */
         this.frustum[5][0] = clip[ 3] + clip[ 2];
         this.frustum[5][1] = clip[ 7] + clip[ 6];
         this.frustum[5][2] = clip[11] + clip[10];
         this.frustum[5][3] = clip[15] + clip[14];
 
         /* Normalize the result */
         t = (float) Math.sqrt(this.frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
         this.frustum[5][0] /= t;
         this.frustum[5][1] /= t;
         this.frustum[5][2] /= t;
         this.frustum[5][3] /= t;
     }
 }
