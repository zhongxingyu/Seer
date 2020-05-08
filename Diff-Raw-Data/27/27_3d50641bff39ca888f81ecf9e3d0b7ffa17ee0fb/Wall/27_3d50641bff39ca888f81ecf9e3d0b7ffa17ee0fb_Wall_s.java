 package com.tgra;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL11;
 
 public class Wall {
     private int row;
     private int column;
     private boolean front;
     
	public Wall(int row, int column, boolean front) {
 		this.row = row;
 		this.column = column;
 		this.front = front;
 	}
 	public int rowcol(float z) {
 		return (int)(((z + 100)/200)*20);
 	}
 	public void preventCollision(Camera camera) {
 		if(camera.eye.y < 0 || camera.eye.y > 10)
 			return;
 		int camera_col = rowcol(camera.eye.z);
 		int camera_row = rowcol(camera.eye.x);
 
 		if(this.front) {
 		//FRONT APPROACH
 		int lowZLimit = -100 + this.row*10;
 		int highZLimit = lowZLimit + 10;
 		int XLimit = -100 + (this.column+1)*10;
 
 			if( (Math.ceil(camera.eye.x) == XLimit+1 || Math.ceil(camera.eye.x) == XLimit-1 || Math.ceil(camera.eye.x) == XLimit)
 					&& Math.ceil(camera.eye.z) <= highZLimit 
 					&& Math.ceil(camera.eye.z) >= lowZLimit) {
 			   if(camera_row == this.column)
 			    camera.eye.x -= 1;
 			   else
 				camera.eye.x += 1;
 			}		
 		}
 		else {
 			int lowXLimit = -100 + (this.row) * 10;
 			int highXLimit = lowXLimit +10;
 			int ZLimit = -100 + ((this.column)*10) + 10;
 			System.out.println(ZLimit + " " + camera.eye.z);
 			if( (Math.ceil(camera.eye.z) == ZLimit+1 || Math.ceil(camera.eye.z) == ZLimit-1|| Math.ceil(camera.eye.z) == ZLimit)
 					&& Math.ceil(camera.eye.x)<= highXLimit 
 					&& Math.ceil(camera.eye.x) >= lowXLimit) {
 			   if(camera_col == this.column)
 			    camera.eye.z -= 1;
 			   else
 				camera.eye.z += 1;
 			}		
 			
 		}
 	}
 	public void display() {
 		this.display(this.row,this.column,this.front);
 	}
 	public void display(int row, int column,boolean across) {
 		if(across) {
 			row = 19-row;
 		}
 		Gdx.gl11.glPushMatrix();
 
 		  row = -100+10*(row);
 		  column = -100+10*(column+1);
 			if(across) {
 				  Gdx.gl11.glRotatef(90, 0,1, 0);
 				}
 
 		int j = 0;
 
 		for(int k=-100;k<100;k+=10) {
 			if(k != column)
 				continue;
 		for(int i=-100;i<100;i+=10) {
 			if(i != row)
 				continue;
 		Gdx.gl11.glPushMatrix();
 
 		Gdx.gl11.glTranslatef(i,0f,k);
 
 		float[] materialDiffuse = {0.2f, 7.3f, 7.0f, 0.0f};
 		float[] materialDiffuse2 = {6.2f, 0.3f, 7.0f, 0.0f};
         if(j % 2 == 0)
 		  Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialDiffuse, 0);
         else
           Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialDiffuse2, 0);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
 		Gdx.gl11.glPopMatrix();
 		j++;
 		}
 		}
 		Gdx.gl11.glPopMatrix();
 	}
 }
