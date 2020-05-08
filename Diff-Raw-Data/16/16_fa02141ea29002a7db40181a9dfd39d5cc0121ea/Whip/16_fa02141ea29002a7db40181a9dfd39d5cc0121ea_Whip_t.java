 package nitwhips.whipulator;
 
 import java.awt.event.MouseEvent;
 
 public class Whip {
 	
 	/* Static constants */
 	
 	static int MAX_MODES = 5;
 	static int HEIGHT = 50;
 	static int RADIUS = 1;
 
 	/* Drawing */
 		
 	Whipulator p;
 	boolean selected;
 
 	/* Whip information */
 
 	int id;
 	float base_x, base_y;
 
 	/* Controls */
 
 	float[][] tip_history;
 	float[] tip_max;
 	int tip_history_index;
 
 	int tip_wave_x, tip_wave_y, tip_wave_height, tip_wave_width;
 
 	int mode_control_x, mode_control_y, mode_control_height, mode_control_width, mode_control_padding, mode_control_band_width;
 
 	Button[] presets;
 	Button reset;
 
 	/* Sensor data */
 
 	float tip_x, tip_y;
 	
 	float[][][] modes;
 	//Modes are roughly the fourier tansform of the x and y accelerometer signal. 
 	//The 0th mode is the DC offset or the lean of the pole.
 	//In reality this will be a little trickier to compute
 	
 	SimEngine engine;
 	
 	/* LED Output */
 	
 	int[][][] leds;
 	
 	public Whip(Whipulator p, int id, float base_x, float base_y) {
 		/* Drawing */
 	
 		this.p = p;
 		selected = false;
 		
 		/* Whip information */
 	
 		this.id = id;
 		this.base_x = base_x;
 		this.base_y = base_y;
 		
 		/* Controls */
 	
 		tip_history = new float[0][2];
 		tip_max = new float[3]; //x,y,amplitude
 		tip_history_index = 0;
 	
 		tip_wave_width = 350;
 		tip_wave_height = 250;
 		tip_wave_x = p.width-tip_wave_width;
 		tip_wave_y = p.height-tip_wave_height;
 		
 		mode_control_width = p.width-tip_wave_width;
 		mode_control_height = tip_wave_height;
 		mode_control_x = 0;
 		mode_control_y = p.height-mode_control_height;
 		mode_control_padding = 3;
 		mode_control_band_width = mode_control_width/MAX_MODES/4;
 		
 		p.registerMouseEvent(this);
 		
 		presets = new Button[5];
 		
 		for(int i = 0;i < presets.length;i++) {
 			final int tmp = i;
 			
 			presets[i] = new Button(p, tip_wave_x+30*i, tip_wave_y-30, new EventListener() {
 				public void event(int arg) {
 					for(int j = 0;j < presets.length;j++) presets[j].clear();
 					reset.clear();
 					resetModes();
					setMode(tmp, 1, 1, -1, 1);
 				}
 			});
 			presets[i].setVisible(false);
 		}
 		
 		reset = new Button(p, tip_wave_x+30*presets.length, tip_wave_y-30, new EventListener() {
 			public void event(int arg) {
 				for(int j = 0;j < presets.length;j++) presets[j].clear();
 				reset.clear();
 				resetModes();
 			}
 		});
 		
 		reset.setVisible(false);
 
 		/* Sensor data */
 		
 		tip_x = 0;
 		tip_y = 0;
 
 		modes = new float[MAX_MODES][2][2]; //[mode][x/y][real/complexe] (where real is cos and complexe is sin)
 		
 		engine = new SimEngine();
 		
 		/* LED Output */
 
 		leds = new int[5][4][3]; //[panel][face][RGB]
 	}
 	
 	public void drawModel() {
 		p.pushMatrix();
 		p.translate(base_x, 0, base_y);
 		
 		for(int i = 0;i < leds.length;i++) {
 			drawPanel(i, leds[i]);
 		}
 		
 		p.popMatrix();
 	}
 	
 	public void drawControls() {
 		reset.draw();
 		drawModeControls();
 		drawTipWave();
 	}
 
 	public void drawTipWave() {
 		if(isSelected()) {
 			if(tip_history.length != tip_wave_width) {
 				tip_history = new float[tip_wave_width][2];
				tip_max = new float[3];
 				tip_history_index = 0;
 			}
 
 			p.strokeWeight(3);
 			p.stroke(100);
 			p.fill(0);
 			p.rect(tip_wave_x, tip_wave_y, tip_wave_width-1, tip_wave_height-1);
 			
 			int circle_width = p.min(tip_wave_height, tip_wave_width);
 			int circle_x = tip_wave_x+tip_wave_width/2;
 			int circle_y = tip_wave_y+tip_wave_height/2;
 			p.ellipse(circle_x, circle_y, circle_width, circle_width);
 		
 			p.strokeWeight(3);
 			p.stroke(100);
 			
 			for(int i = 0;i < tip_history.length;i++) {
 				p.stroke(p.map(i, 0, tip_history.length, 0, 100));
 				p.point(
 					circle_x+p.map(tip_history[(tip_history_index+i)%tip_history.length][1]/tip_max[2], -1, 1, -circle_width/2, circle_width/2),
 					circle_y+p.map(tip_history[(tip_history_index+i)%tip_history.length][0]/tip_max[2], -1, 1, -circle_width/2, circle_width/2)
 				);
 			}
 
 			// p.rect(tip_wave_x, tip_wave_y, tip_wave_width-1, tip_wave_height/2-1);
 			// p.rect(tip_wave_x, tip_wave_y+tip_wave_height/2, tip_wave_width-1, tip_wave_height/2-1);
 			// 		
 			// p.strokeWeight(3);
 			// p.stroke(100);
 			// 		
 			// for(int i = 0;i < tip_history.length;i++) {
 			// 	p.point(i+tip_wave_x, tip_wave_y+p.map(tip_history[(tip_history_index+i)%tip_history.length][0], -tip_max[0], tip_max[0], 5, tip_wave_height/2-5));
 			// 	p.point(i+tip_wave_x, tip_wave_y+tip_wave_height/2+p.map(tip_history[(tip_history_index+i)%tip_history.length][1], -tip_max[1], tip_max[1], 5, tip_wave_height/2-5));
 			// }
 			// 
 			// p.fill(255);
 			// p.stroke(255);
 			// p.text("X", tip_wave_x+5, tip_wave_y+15);
 			// p.text("Y", tip_wave_x+5, tip_wave_y+tip_wave_height/2+15);
			
			p.fill(255);
			p.stroke(255);
			p.text("PRESETS", tip_wave_x, tip_wave_y-35);
 		}
 	}
 	
 	public void drawModeControls() {
 		if(isSelected()) {
 	
 			p.strokeWeight(3);
 			p.stroke(100);
 			p.fill(0);
 			p.rect(mode_control_x, mode_control_y, mode_control_width-1, mode_control_height-1);
 	
 			p.strokeWeight(1);
 									
 			for(int i = 0;i < MAX_MODES;i++) {
 				for(int j = 0;j < 2;j++) {
 					if(j%2 == 0) {
 						p.stroke(0);
 						p.fill(0);
 					} else {
 						p.stroke(35);
 						p.fill(35);
 					}
 					
 					p.rect(
 						mode_control_x+mode_control_padding+(i*4+j*2)*mode_control_band_width,
 						mode_control_y+mode_control_padding,
 						2*mode_control_band_width-mode_control_padding,
 						mode_control_height-2*mode_control_padding
 					);
 					
 					for(int k = 0;k < 2;k++) {
 						p.stroke(255);
 						p.fill(255);
 			
 						p.rect(
 							mode_control_x+mode_control_padding+(i*4+j*2+k)*mode_control_band_width,
 							mode_control_y+mode_control_height/2,
 							mode_control_band_width-mode_control_padding,
 							p.map(
 								modes[i][j][k], 
 								-1, 
 								1, 
 								-(mode_control_height-2*mode_control_padding)/2,
 								(mode_control_height-2*mode_control_padding)/2
 							)
 						);
 					}
 				}
			}		
 		}
 	}
 	
 	void drawPanel(int panel, int face_colors[][]) {
 		if(!selected) p.stroke(100);
 		else p.stroke(255);
 		p.strokeWeight(3);
 
 		int panel_height = HEIGHT/leds.length;
 
 		p.beginShape(p.QUADS);
 
 		p.fill(face_colors[0][0], face_colors[0][1], face_colors[0][2]); 
 
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2+RADIUS);
 
 		p.fill(face_colors[1][0], face_colors[1][1], face_colors[1][2]); 
 
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2+RADIUS);
 
 		p.fill(face_colors[2][0], face_colors[2][1], face_colors[2][2]); 
 
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2-RADIUS);
 
 		p.fill(face_colors[3][0], face_colors[3][1], face_colors[3][2]); 
 
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2-RADIUS);
 
 		p.fill(100);        
 
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2-RADIUS, -(panel+1)*panel_height, base_y/2+RADIUS);
 
 		p.fill(100);        
 
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2-RADIUS);
 		p.vertex(base_x/2+RADIUS, -panel*panel_height, base_y/2+RADIUS);
 		p.vertex(base_x/2-RADIUS, -panel*panel_height, base_y/2+RADIUS);
 
 		p.endShape();
 		
 		p.strokeWeight(1);
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	public void selected(boolean selected) {
 		this.selected = selected;
 		for(int i = 0;i < presets.length;i++) presets[i].setVisible(selected);
 		reset.setVisible(selected);
 	}
 	
 	public void setMode(int mode, float real_value_x, float img_value_x, float real_value_y, float img_value_y) {
 		if(mode >= 0 && mode < MAX_MODES) {
 			modes[mode][0][0] = real_value_x;
 			modes[mode][0][1] = img_value_x;
 			modes[mode][1][0] = real_value_y;
 			modes[mode][1][1] = img_value_y;
 		} else {
 			System.err.println("Invalid mode data given");
 		}
 	}
 	
 	public void resetModes() {
 		modes = new float[MAX_MODES][2][2];
 	}
 	
 	public void setModes(float[][][] modes) {
 		if(modes.length >= MAX_MODES && modes[0].length >= 2 && modes[0][0].length >= 2) this.modes = modes;
 	}
 	
 	public float[][] getMode(int mode) {
 		return modes[mode];
 	}
 
 	public float getMode(int mode, int x_y, int real_img) {
 		return modes[mode][x_y][real_img];
 	}
 	
 	public void mouseEvent(MouseEvent event) {
 		if(isSelected()) {
 			if(event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == MouseEvent.MOUSE_DRAGGED) {
 				int mouseX = event.getX();
 				int mouseY = event.getY();
 				for(int i = 0;i < MAX_MODES;i++) {
 					for(int j = 0;j < 2;j++) {
 						for(int k = 0;k < 2;k++) {
 							if(
 								mouseX > mode_control_x+(i*4+j*2+k)*mode_control_band_width &&
 								mouseX < mode_control_x+(i*4+j*2+k+1)*mode_control_band_width &&
 								mouseY > mode_control_y &&
 								mouseY < mode_control_y+mode_control_height
 							) {
 								modes[i][j][k] = p.map(
 									mouseY-mode_control_y-mode_control_padding,
 									0,
 									(mode_control_height-mode_control_padding),
 									-1,
 									1
 								);
 								for(int l = 0;l < presets.length;l++) presets[l].clear();
 								reset.clear();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	class SimEngine implements Runnable {
 		
 		Thread sim_thread;
 		boolean running = true;
 		int start_time;
 
 		public SimEngine() {
 			running = true;
 		
 			start_time = p.millis();
 		
 			sim_thread = new Thread(this);
 			sim_thread.setDaemon(true);
 			sim_thread.start();
 		}
 
 		public void run() {
 			while(running) {
 				int time = p.millis()-start_time;
 				tip_x = 0;
 				tip_y = 0;
 				
 				for(int i = 0;i < MAX_MODES;i++) {
 					tip_x += modes[i][0][0]*p.cos(p.HALF_PI*i*time/1000)+modes[i][0][1]*p.sin(p.HALF_PI*i*time/1000);
 					tip_y += modes[i][1][0]*p.cos(p.HALF_PI*i*time/1000)+modes[i][1][1]*p.sin(p.HALF_PI*i*time/1000);
 				}
 				
 				if(tip_history.length != 0) {
 					tip_history[tip_history_index%tip_history.length][0] = tip_x;
 					tip_history[tip_history_index%tip_history.length][1] = tip_y;
 
 					if(p.abs(tip_x) > tip_max[0]) tip_max[0] = p.abs(tip_x);
 					if(p.abs(tip_y) > tip_max[1]) tip_max[1] = p.abs(tip_y);
 
					float amp = p.sqrt(p.sq(tip_x)+p.sq(tip_y));
 					if(amp > tip_max[2]) tip_max[2] = amp;
 					
 					tip_history_index++;
 				}
 				
  				try {
 					Thread.sleep(1);
 				} catch(Exception e) {
 					
 				}
 			}
 		}
 		
 		public void stop() {
 			running = false;
 			while(sim_thread.isAlive()) Thread.yield();
 		}
 		
 	}
 
 }
