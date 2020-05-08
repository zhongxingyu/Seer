 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.zip.Inflater;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 
 import javax.swing.JRadioButton;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 
 
 public class SkyDetector extends JFrame{
 
 	private static final long serialVersionUID=-681141109762206171L;
 
 	enum BlockSize {
 		_4x4(2),
 		_8x8(3),
 		_16x16(4);
 
 		protected int log;
 
 		BlockSize(int _log) {
 			log=_log;
 		}
 
 	};
 
 	enum Norm {
 		L1,
 		L2
 	};
 
 	protected int[] luma;
 
 	protected BufferedImage disp;
 
 	protected double[] norms;
 
 	protected BlockSize bsize=BlockSize._4x4;
 
 	protected Norm norm=Norm.L1;
 
 	protected JLabel imageLabel;
 
 	protected JTextField thresholdBox;
 	
 	protected JSlider thresholdSlider;
 
 	protected ButtonGroup blockSizeButtonGroup;
 
 	private ButtonGroup normButtonGroup;
	
	protected int threshold = 24;
 
 	public static int[] getLumaPlane(BufferedImage _img) {
 		int[] luma=new int[_img.getWidth()*_img.getHeight()];
 		for (int y=0;y<_img.getHeight();y++) {
 			for (int x=0;x<_img.getWidth();x++) {
 				int rgb=_img.getRGB(x,y);
 				int r=(rgb>>16)&0xff;
 				int g=(rgb>>8)&0xff;
 				int b=(rgb>>0)&0xff;
 				luma[y*_img.getWidth()+x]=(int)Math.max(0,Math.min(255,0.299*r+0.587*g+0.114*b+16+0.5));
 			}
 		}
 		return(luma);
 	}
 
 	protected void loadImage(File _file) throws IOException {
 		BufferedImage img=ImageIO.read(_file);
 		luma=getLumaPlane(img);
 		disp=new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
 		imageLabel.setIcon(new ImageIcon(disp));
 		updateNorms();
 		pack();
 	}
 
 	protected void updateNorms() {
 		norms=computeNorms(luma,disp.getWidth(),disp.getHeight(),bsize,norm);
 		double max=0;
 		for (int i=0;i<norms.length;i++) {
 			max=Math.max(max,norms[i]);
 		}
 		thresholdSlider.setMaximum((int)Math.ceil(Math.sqrt(max)));
 		refresh();
 	}
 
 	protected void refresh() {
 		int n=1<<bsize.log;
 		int nx=disp.getWidth()/n;
 		int ny=disp.getHeight()/n;
 		for (int y=0;y<disp.getHeight();y++) {
 			for (int x=0;x<disp.getWidth();x++) {
 				int Y=luma[y*disp.getWidth()+x];
 				int rgb=(Y<<16)|(Y<<8)|Y;
 				int bx=x/n;
 				int by=y/n;
				if (bx<nx&&by<ny&&norms[by*nx+bx]<threshold) {
 					rgb&=0xffff00;
 				}
 				disp.setRGB(x,y,rgb);
 			}
 		}
 
 		//repaint(disp,luma,disp.getWidth(),disp.getHeight(),bsize,norms,threshold.getValue());
 		repaint();
 	}
 	
 	
 
 	protected boolean[] makeTextureMap() {
 		int n=1<<bsize.log;
 		int nx=disp.getWidth()/n;
 		int ny=disp.getHeight()/n;
 		boolean [] mask = new boolean [nx *ny];
 		
 		for (int y=0;y<ny;y++) {
 			for (int x=0;x<nx;x++) {
				if (x<nx&&y<ny&&norms[y*nx+x]<threshold) {
 					mask[y * nx +x] = true;
 				}
 				
 			}
 		}
 		return mask;
 	}
 	
 	private void floodFill(boolean[][] skymask, boolean[][] confirmed, int x, int y){
 		System.out.println("x "+x+" y "+y);
 		
 		if (0 <= y && y < skymask[0].length && 0 <= x && x < skymask.length && ((!confirmed[x][y]) && (skymask [x][y]))){
 		confirmed[x][y] = true;
 		floodFill(skymask, confirmed,x+1 ,y );
 		floodFill(skymask, confirmed,x-1 ,y );
 		floodFill(skymask, confirmed,x ,y+1 );
 		floodFill(skymask, confirmed,x ,y-1 );
 		}
 	}
 
 	
 	private void floodFill(boolean[] skymask, boolean[] confirmed, Stack<Integer> pStack, int mx, int my){
 		int y = pStack.pop();
 		int x = pStack.pop();
 		if (0 <= y && y < my && 0 <= x && x < mx && ((!confirmed[y * mx + x]) && (skymask [y * mx + x]))){
 		confirmed[y * mx + x] = true;
 		pStack.push(x+1);		pStack.push(y);
 		pStack.push(x-1);		pStack.push(y);
 		pStack.push(x);		pStack.push(y+1);
 		pStack.push(x);		pStack.push(y-1);
 		}
 	}
 	
 	protected boolean[] floodFillFromTop(boolean[] skymask){
 	int n=1<<bsize.log;
 	int mx=disp.getWidth()/n;
 	int my=disp.getHeight()/n;
 	Stack<Integer> pStack = new Stack<Integer>();
 	boolean [] fillMask = new boolean[skymask.length];
 	for (int x=0;x< mx ;x++) {
 		//System.out.println(fillMask[0][y]);
 		pStack.push(x);
 		pStack.push(0);
 		
 		
 	}
 	while(!pStack.empty()){
 		floodFill(skymask, fillMask, pStack, mx, my);
 	}
 	
 	return fillMask;
 }
 
 
 	
 	protected void drawMask(boolean[] skymask)
 	{
 	int n=1<<bsize.log;
 	int nx=disp.getWidth()/n;
 	int ny=disp.getHeight()/n;
 	
 	for (int y=0;y<disp.getHeight();y++) {
 		for (int x=0;x<disp.getWidth();x++) {
 			int Y=luma[y*disp.getWidth()+x];
 			int rgb=(Y<<16)|(Y<<8)|Y;
 			int bx=x/n;
 			int by=y/n;
 			if (bx<nx&&by<ny&& skymask[by * nx + bx]) {
 				rgb|=0x00ff00;
 			}
 			disp.setRGB(x,y,rgb);
 		}
 	}
 	repaint();
 	}
 	protected void testFindSky(){
 		
 		boolean[] mask = makeTextureMap();
 		mask = floodFillFromTop(mask);
 		drawMask(mask);
 		
 	}
 
 
 	public SkyDetector(String fileURL) throws IOException {
 		getContentPane().setLayout(new FlowLayout());
 
 		setResizable(false);
 		imageLabel=new JLabel();
 		getContentPane().add(imageLabel);
 
 		JPanel panel=new JPanel();
 		panel.setLayout(new GridBagLayout());
 		getContentPane().add(panel);
 
 		GridBagConstraints gbc=new GridBagConstraints();
 		gbc.insets=new Insets(10,0,0,0);
 		gbc.anchor=GridBagConstraints.NORTHEAST;
 		gbc.gridwidth=GridBagConstraints.RELATIVE;
 		panel.add(new JLabel("Block Size:"),gbc);
 
 		blockSizeButtonGroup=new ButtonGroup();
 
 		JPanel blockSizePanel=new JPanel();
 		blockSizePanel.setLayout(new GridLayout(3,0));
 		gbc.anchor=gbc.anchor=GridBagConstraints.WEST;
 		gbc.gridwidth=GridBagConstraints.REMAINDER;
 		panel.add(blockSizePanel,gbc);		
 
 		JRadioButton blockSize4x4=new JRadioButton("4x4");
 
 		blockSize4x4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				bsize=BlockSize._4x4;
 				updateNorms();
 			}
 		});
 		blockSizeButtonGroup.add(blockSize4x4);
 		blockSizePanel.add(blockSize4x4);
 
 		JRadioButton blockSize8x8=new JRadioButton("8x8");
 		blockSize8x8.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				bsize=BlockSize._8x8;
 				updateNorms();
 			}
 		});
 		blockSizeButtonGroup.add(blockSize8x8);
 		blockSizePanel.add(blockSize8x8);
 
 		JRadioButton blockSize16x16=new JRadioButton("16x16");
 		blockSize16x16.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				bsize=BlockSize._16x16;
 				updateNorms();
 			}
 		});
 		blockSizeButtonGroup.add(blockSize16x16);
 		blockSizePanel.add(blockSize16x16);
 
 		gbc.anchor=GridBagConstraints.NORTHEAST;
 		gbc.gridwidth=GridBagConstraints.RELATIVE;
 		panel.add(new JLabel("Norm:"),gbc);
 
 		JPanel normPanel=new JPanel();
 		normPanel.setLayout(new GridLayout(2,0));
 		gbc.anchor=gbc.anchor=GridBagConstraints.WEST;
 		gbc.gridwidth=GridBagConstraints.REMAINDER;
 		panel.add(normPanel,gbc);
 
 		normButtonGroup=new ButtonGroup();
 
 		JRadioButton l1RadioButton=new JRadioButton("L1");
 		l1RadioButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				norm=Norm.L1;
 				updateNorms();
 			}
 		});
 		normButtonGroup.add(l1RadioButton);
 		normPanel.add(l1RadioButton);
 
 		JRadioButton l2RadioButton=new JRadioButton("L2");
 		l2RadioButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				norm=Norm.L2;
 				updateNorms();
 			}
 		});
 		normButtonGroup.add(l2RadioButton);
 		normPanel.add(l2RadioButton);
 
 		gbc.anchor=GridBagConstraints.NORTHEAST;
 		gbc.gridwidth=GridBagConstraints.RELATIVE;
 		panel.add(new JLabel("Threshold:"),gbc);
 
 		JPanel thresholdPanel=new JPanel(new GridLayout(2,0));
 		gbc.anchor=gbc.anchor=GridBagConstraints.WEST;
 		gbc.gridwidth=GridBagConstraints.REMAINDER;
 		panel.add(thresholdPanel,gbc);
 
 		thresholdSlider=new JSlider(JSlider.HORIZONTAL,0,500,0);
 		thresholdSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
				threshold = (int)Math.pow(thresholdSlider.getValue(),2);
				thresholdBox.setText("" +threshold);
 				refresh();
 			}
 		});
 	
 		thresholdPanel.add(thresholdSlider);
 		
 		thresholdBox = new JTextField();
 		thresholdBox.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 				threshold = Integer.parseInt(thresholdBox.getText());
 				} catch (java.lang.NumberFormatException e){
 					thresholdBox.setText("" + (int)Math.pow(thresholdSlider.getValue(),2));
 					return;
 				}
				thresholdBox.setText("" + threshold);
 				refresh();
 			}});
 		
 		thresholdPanel.add(thresholdBox);
 		
 		JButton loadImage = new JButton("Load Image");
 		loadImage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				JFileChooser theFileToLoad = new JFileChooser();
 		       
 		        int returnVal = theFileToLoad.showOpenDialog(null);
 		        if(returnVal == JFileChooser.APPROVE_OPTION) {
 		        	try {
 						loadImage(theFileToLoad.getSelectedFile());
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 		        }
 				
 			}
 		});
 		panel.add(loadImage);
 		
 		JButton testFlood = new JButton("flood for sky");
 		testFlood.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent _ae) {
 				testFindSky();
 			}
 		});
 		panel.add(testFlood);
 		
 		loadImage(new File(fileURL));
 
 		blockSize4x4.setSelected(true);
 		l1RadioButton.setSelected(true);
 	}
 	
 
 
 	public static void od_bin_fdct4(int[] _y,int _yoff,int[] _x,int _xoff,int _xstride){
 		int t0;
 		int t1;
 		int t2;
 		int t2h;
 		int t3;
 		/*Initial permutation:*/
 		t0=_x[_xoff+0*_xstride];
 		t2=_x[_xoff+1*_xstride];
 		t1=_x[_xoff+2*_xstride];
 		t3=_x[_xoff+3*_xstride];
 		/*+1/-1 butterflies:*/
 		t3=t0-t3;
 		t2+=t1;
 		t2h=t2>>1;
 		t1=t2h-t1;
 		t0-=t3>>1;
 		/*+ Embedded 2-point type-II DCT.*/
 		t0+=t2h;
 		t2=t0-t2;
 		/*+ Embedded 2-point type-IV DST.*/
 		/*23013/32768~=4*sin(\frac{\pi}{8})-2*tan(\frac{\pi}{8})~=0.70230660471416898931046248770220*/
 		t3-=(t1*23013+16384)>>15;
 		/*21407/32768~=\sqrt{1/2}*cos(\frac{\pi}{8}))~=0.65328148243818826392832158671359*/
 		t1+=(t3*21407+16384)>>15;
 		/*18293/16384~=4*sin(\frac{\pi}{8})-tan(\frac{\pi}{8})~=1.1165201670872640381121512119119*/
 		t3-=(t1*18293+8192)>>14;
 		_y[_yoff+0]=t0;
 		_y[_yoff+1]=t1;
 		_y[_yoff+2]=t2;
 		_y[_yoff+3]=t3;
 	}
 
 	public static void od_bin_fdct4x4(int[] _y,int _yoff,int _ystride,int[] _x,int _xoff,int _xstride){
 		int[] z=new int[4*4];
 		for(int i=0;i<4;i++)od_bin_fdct4(z,4*i,_x,_xoff+i,_xstride);
 		for(int i=0;i<4;i++)od_bin_fdct4(_y,_yoff+_ystride*i,z,i,4);
 	}
 
 	public static void od_bin_fdct8(int[] _y,int _yoff,int[] _x,int _xoff,int _xstride){
 		/*31 adds, 5 shifts, 15 "muls".*/
 		/*The minimum theoretical number of multiplies for a uniformly-scaled 8-point
 		   transform is 11, but the best I've been able to come up with for a
 		   reversible version with orthonormal scaling is 15.
 		  We pick up 3 multiplies when computing the DC, since we have an odd number
 		   of summation stages, leaving no chance to cancel the asymmetry in the last
 		   one.
 		  Instead, we have to implement it as a rotation by \frac{\pi}{4} using
 		   lifting steps.
 		  We pick up one more multiply when computing the Type IV DCT in the odd
 		   half.
 		  This comes from using 3 lifting steps to implement another rotation by
 		   \frac{\pi}{4} (with asymmetrically scaled inputs and outputs) instead of
 		   simply scaling two values by \sqrt{2}.*/
 		int t0;
 		int t1;
 		int t1h;
 		int t2;
 		int t3;
 		int t4;
 		int t4h;
 		int t5;
 		int t6;
 		int t6h;
 		int t7;
 		/*Initial permutation:*/
 		t0=_x[_xoff+0*_xstride];
 		t4=_x[_xoff+1*_xstride];
 		t2=_x[_xoff+2*_xstride];
 		t6=_x[_xoff+3*_xstride];
 		t7=_x[_xoff+4*_xstride];
 		t3=_x[_xoff+5*_xstride];
 		t5=_x[_xoff+6*_xstride];
 		t1=_x[_xoff+7*_xstride];
 		/*+1/-1 butterflies:*/
 		t1=t0-t1;
 		t1h=t1>>1;
 		t0-=t1h;
 		t4+=t5;
 		t4h=t4>>1;
 		t5-=t4h;
 		t3=t2-t3;
 		t2-=t3>>1;
 		t6+=t7;
 		t6h=t6>>1;
 		t7=t6h-t7;
 		/*+ Embedded 4-point type-II DCT.*/
 		t0+=t6h;
 		t6=t0-t6;
 		t2=t4h-t2;
 		t4=t2-t4;
 		/*|-+ Embedded 2-point type-II DCT.*/
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t0-=(t4*13573+16384)>>15;
 		/*11585/16384~=\sqrt{\frac{1}{2}}~=0.70710678118654752440084436210485*/
 		t4+=(t0*11585+8192)>>14;
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t0-=(t4*13573+16384)>>15;
 		/*|-+ Embedded 2-point type-IV DST.*/
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{\sin(\frac{3\pi}{8})}~=
 		   0.66817863791929891999775768652308*/
 		t6-=(t2*21895+16384)>>15;
 		/*15137/16384~=sin(\frac{3\pi}{8})~=0.92387953251128675612818318939679*/
 		t2+=(t6*15137+8192)>>14;
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{\sin(\frac{3\pi}{8})}~=
 		   0.66817863791929891999775768652308*/
 		t6-=(t2*21895+16384)>>15;
 		/*+ Embedded 4-point type-IV DST.*/
 		/*19195/32768~=2-\sqrt{2}~=0.58578643762690495119831127579030*/
 		t3+=(t5*19195+16384)>>15;
 		/*11585/16384~=\sqrt{\frac{1}{2}}~=0.70710678118654752440084436210485*/
 		t5+=(t3*11585+8192)>>14;
 		/*29957/32768~=\sqrt{2}-\frac{1}{2}~=0.91421356237309504880168872420970*/
 		t3-=(t5*29957+16384)>>15;
 		t7=(t5>>1)-t7;
 		t5-=t7;
 		t3=t1h-t3;
 		t1-=t3;
 		/*3227/32768~=\frac{1-cos(\frac{\pi}{16})}{sin(\frac{\pi}{16})}~=
 		   0.098491403357164253077197521291327*/
 		t7+=(t1*3227+16384)>>15;
 		/*6393/32768~=sin(\frac{\pi}{16})~=0.19509032201612826784828486847702*/
 		t1-=(t7*6393+16384)>>15;
 		/*3227/32768~=\frac{1-cos(\frac{\pi}{16})}{sin(\frac{\pi}{16})}~=
 		   0.098491403357164253077197521291327*/
 		t7+=(t1*3227+16384)>>15;
 		/*2485/8192~=\frac{1-cos(\frac{3\pi}{16})}{sin(\frac{3\pi}{16})}~=
 		   0.30334668360734239167588394694130*/
 		t5+=(t3*2485+4096)>>13;
 		/*18205/32768~=sin(\frac{3\pi}{16})~=0.55557023301960222474283081394853*/
 		t3-=(t5*18205+16384)>>15;
 		/*2485/8192~=\frac{1-cos(\frac{3\pi}{16})}{sin(\frac{3\pi}{16})}~=
 		   0.30334668360734239167588394694130*/
 		t5+=(t3*2485+4096)>>13;
 		_y[_yoff+0]=t0;
 		_y[_yoff+1]=t1;
 		_y[_yoff+2]=t2;
 		_y[_yoff+3]=t3;
 		_y[_yoff+4]=t4;
 		_y[_yoff+5]=t5;
 		_y[_yoff+6]=t6;
 		_y[_yoff+7]=t7;
 	}
 
 	public static void od_bin_fdct8x8(int[] _y,int _yoff,int _ystride,int[] _x,int _xoff,int _xstride){
 		int[] z=new int[8*8];
 		for(int i=0;i<8;i++)od_bin_fdct8(z,8*i,_x,_xoff+i,_xstride);
 		for(int i=0;i<8;i++)od_bin_fdct8(_y,_yoff+_ystride*i,z,i,8);
 	}
 
 	public static void od_bin_fdct16(int[] _y,int _yoff,int[] _x,int _xoff,int _xstride){
 		/*83 adds, 16 shifts, 33 "muls".*/
 		/*The minimum theoretical number of multiplies is 26~\cite{DH87}, but the
 		 best practical algorithm I know is 31~\cite{LLM89}.
 		This is a modification of the Loeffler et al. factorization that allows us
 		 to have a reversible integer transform with true orthonormal scaling.
 		This required some major reworking of the odd quarter of the even half
 		 (the 4-point Type IV DST), and added two multiplies in order to implement
 		 two rotations by \frac{\pi}{4} with lifting steps (requiring 3 multiplies
 		 instead of the normal 2).
 		@INPROCEEDINGS{DH87,
 		  author={Pierre Duhamel and Hedi H'Mida},
 		  title="New 2^n {DCT} Algorithms Suitable for {VLSI} Implementation",
 		  booktitle="Proc. $12^\textrm{th}$ International Conference on Acoustics,
 		   Speech, and Signal Processing (ICASSP'87)",
 		  volume=12,
 		  pages="1805--1808",
 		  address="Issy-les-Moulineaux, France",
 		  month=Apr,
 		  year=1987
 		}
 		@INPROCEEDINGS{LLM89,
 		  author="Christoph Loeffler and Adriaan Lightenberg and
 		   George S. Moschytz",
 		  title="Practical Fast {1-D} {DCT} Algorithms with 11 Multiplications",
 		  booktitle="Proc. $14^\textrm{th}$ International Conference on Acoustics,
 		   Speech, and Signal Processing (ICASSP'89)",
 		  volume=2,
 		  pages="988--991",
 		  address="Zhurich, Switzerland",
 		  month=May,
 		  year=1989
 		}*/
 		int t0;
 		int t1;
 		int t1h;
 		int t2;
 		int t2h;
 		int t3;
 		int t4;
 		int t5;
 		int t6;
 		int t7;
 		int t8;
 		int t8h;
 		int t9;
 		int ta;
 		int tah;
 		int tb;
 		int tbh;
 		int tc;
 		int tch;
 		int td;
 		int tdh;
 		int te;
 		int tf;
 		int tfh;
 		/*Initial permutation:*/
 		t0=_x[_xoff+0*_xstride];
 		t8=_x[_xoff+1*_xstride];
 		t4=_x[_xoff+2*_xstride];
 		tc=_x[_xoff+3*_xstride];
 		te=_x[_xoff+4*_xstride];
 		ta=_x[_xoff+5*_xstride];
 		t6=_x[_xoff+6*_xstride];
 		t2=_x[_xoff+7*_xstride];
 		t3=_x[_xoff+8*_xstride];
 		td=_x[_xoff+9*_xstride];
 		t9=_x[_xoff+10*_xstride];
 		tf=_x[_xoff+11*_xstride];
 		t1=_x[_xoff+12*_xstride];
 		t7=_x[_xoff+13*_xstride];
 		tb=_x[_xoff+14*_xstride];
 		t5=_x[_xoff+15*_xstride];
 		/*+1/-1 butterflies:*/
 		t5=t0-t5;
 		t8+=tb;
 		t7=t4-t7;
 		tc+=t1;
 		tf=te-tf;
 		ta+=t9;
 		td=t6-td;
 		t2+=t3;
 		t0-=t5>>1;
 		t8h=t8>>1;
 		tb=t8h-tb;
 		t4-=t7>>1;
 		tch=tc>>1;
 		t1=tch-t1;
 		te-=tf>>1;
 		tah=ta>>1;
 		t9=tah-t9;
 		t6-=td>>1;
 		t2h=t2>>1;
 		t3=t2h-t3;
 		/*+ Embedded 8-point type-II DCT.*/
 		t0+=t2h;
 		t6=t8h-t6;
 		t4+=tah;
 		te=tch-te;
 		t2=t0-t2;
 		t8-=t6;
 		ta=t4-ta;
 		tc-=te;
 		/*|-+ Embedded 4-point type-II DCT.*/
 		tc=t0-tc;
 		t8+=t4;
 		t8h=t8>>1;
 		t4=t8h-t4;
 		t0-=tc>>1;
 		/*|-|-+ Embedded 2-point type-II DCT.*/
 		t0+=t8h;
 		t8=t0-t8;
 		/*|-|-+ Embedded 2-point type-IV DST.*/
 		/*32013/32768~=4*sin(\frac{\pi}{8})-2*tan(\frac{\pi}{8})
 		 ~=0.70230660471416898931046248770220*/
 		tc-=(t4*23013+16384)>>15;
 		/*21407~=\sqrt{1/2}*cos(\frac{\pi}{8}))~=0.65328148243818826392832158671359*/
 		t4+=(tc*21407+16384)>>15;
 		/*18293/16384~=4*sin(\frac{\pi}{8})-tan(\frac{\pi}{8})
 		 ~=1.1165201670872640381121512119119*/
 		tc-=(t4*18293+8192)>>14;
 		/*|-+ Embedded 4-point type-IV DST.*/
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t6+=(ta*13573+16384)>>15;
 		/*11585/16384~=\sqrt{\frac{1}{2}}~=0.70710678118654752440084436210485*/
 		ta-=(t6*11585+8192)>>14;
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t6+=(ta*13573+16384)>>15;
 		ta+=te;
 		t2+=t6;
 		te=(ta>>1)-te;
 		t6=(t2>>1)-t6;
 		/*2775/2048~=\frac{\sqrt{2}-cos(\frac{\pi}{16})}{2sin(\frac{\pi}{16})}
 		 ~=1.1108400393486273201524536919723*/
 		te+=(t2*2275+1024)>>11;
 		/*9041/32768~=\sqrt{2}sin(\frac{\pi}{16})
 		 ~=0.27589937928294301233595756366937*/
 		t2-=(te*9041+16384)>>15;
 		/*2873/2048
 		 ~=\frac{cos(\frac{\pi}{16})-\sqrt{\frac{1}{2}}}{sin(\frac{\pi}{16})}
 		 ~=1.4028297067142967321050338435598*/
 		te-=(t2*2873+1024)>>11;
 		/*17185/32768~=\frac{\sqrt{2}-cos(\frac{3\pi}{16})}{2sin(\frac{3\pi}{16})}
 		 ~=0.52445569924008942966043945081053*/
 		t6-=(ta*17185+16384)>>15;
 		/*12873/16384~=\sqrt{2}sin(\frac{3\pi}{16})
 		 ~=0.78569495838710218127789736765722*/
 		ta+=(t6*12873+8192)>>14;
 		/*7335/32768
 		 ~=\frac{cos(\frac{3\pi}{16})-\sqrt{\frac{1}{2}}}{sin(\frac{3\pi}{16})}
 		 ~=0.22384718209265507914012811666071*/
 		t6+=(ta*7335+16384)>>15;
 		/*+ Embedded 8-point type-IV DST.*/
 		/*1035/2048~=\frac{\sqrt{2}-cos(\frac{7\pi}{32})}{2sin(\frac{7\pi}{32})}
 		 ~=0.50536719493782972897642806316664*/
 		t3+=(t5*1035+1024)>>11;
 		/*14699/16384~=\sqrt{2}sin(\frac{7\pi}{32})
 		 ~=0.89716758634263628425064138922084*/
 		t5-=(t3*14699+8192)>>14;
 		/*851/8192
 		~=\frac{cos(\frac{7\pi}{32})-\sqrt{\frac{1}{2}}}{sin(\frac{7\pi}{32}}
 		~=0.10388456785615844342131055214354*/
 		t3-=(t5*851+4096)>>13;
 		/*17515/32768~=\frac{\sqrt{2}-cos(\frac{11\pi}{32})}{2sin(\frac{11\pi}{32})}
 		 ~=0.53452437516842143578098634302964*/
 		tb+=(td*17515+16384)>>15;
 		/*40869/32768~=\sqrt{2}sin(\frac{11\pi}{32})
 		 ~=1.2472250129866712325719902627977*/
 		td-=(tb*40869+16384)>>15;
 		/*4379/16384
 		 ~=\frac{\sqrt{\frac{1}{2}}-cos(\frac{11\pi}{32})}{sin(\frac{11\pi}{32})}
 		 ~=0.26726880719302561523614336238196*/
 		tb+=(td*4379+8192)>>14;
 		/*25809/32768~=\frac{\sqrt{2}-cos(\frac{3\pi}{32})}{2sin(\frac{3\pi}{32})}
 		 ~=0.78762894232967441973847776796517*/
 		t9+=(t7*25809+16384)>>15;
 		/*3363/8192~=\sqrt{2}sin(\frac{3\pi}{32})
 		 ~=0.41052452752235738115636923775513*/
 		t7-=(t9*3363+4096)>>13;
 		/*14101/16384
 		 ~=\frac{cos(\frac{3\pi}{32})-\sqrt{\frac{1}{2}}}{sin(\frac{3\pi}{32})}
 		 ~=0.86065016213948579370059934044795*/
 		t9-=(t7*14101+8192)>>14;
 		/*21669/32768~=\frac{\sqrt{2}-cos(\frac{15\pi}{32})}{2sin(\frac{15\pi}{32})}
 		 ~=0.66128246684651710406296283785232*/
 		t1+=(tf*21669+16384)>>15;
 		/*23059/16384~=\sqrt{2}sin(\frac{15\pi}{32})
 		 ~=1.4074037375263824590260782229840*/
 		tf-=(t1*23059+8192)>>14;
 		/*20055/32768
 		~=\frac{\sqrt{\frac{1}{2}}-cos(\frac{15\pi}{32})}{sin(\frac{15\pi}{32})}
 		~=0.61203676516793497752436407720666*/
 		t1+=(tf*20055+16384)>>15;
 		tf=t3-tf;
 		td+=t9;
 		tfh=tf>>1;
 		t3-=tfh;
 		tdh=td>>1;
 		t9=tdh-t9;
 		t1+=t5;
 		tb=t7-tb;
 		t1h=t1>>1;
 		t5=t1h-t5;
 		tbh=tb>>1;
 		t7-=tbh;
 		t3+=tbh;
 		t5=tdh-t5;
 		t9+=tfh;
 		t7=t1h-t7;
 		tb-=t3;
 		td-=t5;
 		tf=t9-tf;
 		t1-=t7;
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{sin(\frac{3\pi}{8})}
 		 ~=0.66817863791929891999775768652308*/
 		t5-=(tb*21895+16384)>>15;
 		/*15137/16384~=sin(\frac{3\pi}{8})~=0.92387953251128675612818318939679*/
 		tb+=(t5*15137+8192)>>14;
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{sin(\frac{3\pi}{8})}
 		 ~=0.66817863791929891999775768652308*/
 		t5-=(tb*21895+16384)>>15;
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{sin(\frac{3\pi}{8})}
 		 ~=0.66817863791929891999775768652308*/
 		td+=(t3*21895+16384)>>15;
 		/*15137/16384~=sin(\frac{3\pi}{8})~=0.92387953251128675612818318939679*/
 		t3-=(td*15137+8192)>>14;
 		/*21895/32768~=\frac{1-cos(\frac{3\pi}{8})}{sin(\frac{3\pi}{8})}
 		 ~=0.66817863791929891999775768652308*/
 		td+=(t3*21895+16384)>>15;
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t1-=(tf*13573+16384)>>15;
 		/*11585/16384~=\sqrt{\frac{1}{2}}~=0.70710678118654752440084436210485*/
 		tf+=(t1*11585+8192)>>14;
 		/*13573/32768~=\sqrt{2}-1~=0.41421356237309504880168872420970*/
 		t1-=(tf*13573+16384)>>15;
 		_y[_yoff+0]=t0;
 		_y[_yoff+1]=t1;
 		_y[_yoff+2]=t2;
 		_y[_yoff+3]=t3;
 		_y[_yoff+4]=t4;
 		_y[_yoff+5]=t5;
 		_y[_yoff+6]=t6;
 		_y[_yoff+7]=t7;
 		_y[_yoff+8]=t8;
 		_y[_yoff+9]=t9;
 		_y[_yoff+10]=ta;
 		_y[_yoff+11]=tb;
 		_y[_yoff+12]=tc;
 		_y[_yoff+13]=td;
 		_y[_yoff+14]=te;
 		_y[_yoff+15]=tf;
 	}
 
 	public static void od_bin_fdct16x16(int[] _y,int _yoff,int _ystride,int[] _x,int _xoff,int _xstride){
 		int[] z=new int[16*16];
 		for(int i=0;i<16;i++)od_bin_fdct16(z,16*i,_x,_xoff+i,_xstride);
 		for(int i=0;i<16;i++)od_bin_fdct16(_y,_yoff+_ystride*i,z,i,16);
 	}
 
 	public static double[] computeNorms(int[] _luma,int _width,int _height,BlockSize _b_sz,Norm _norm) {
 		int n=1<<_b_sz.log;
 		int nx=_width/n;
 		int ny=_height/n;
 		double[] norms=new double[nx*ny];
 		for (int by=0;by<ny;by++) {
 			for (int bx=0;bx<nx;bx++) {
 				int[] z=new int[n*n];
 				switch (_b_sz) {
 					case _4x4 : {
 						od_bin_fdct4x4(z,0,n,_luma,n*by*_width+n*bx,_width);
 						break;
 					}
 					case _8x8 : {
 						od_bin_fdct8x8(z,0,n,_luma,n*by*_width+n*bx,_width);
 						break;
 					}
 					case _16x16 : {
 						od_bin_fdct16x16(z,0,n,_luma,n*by*_width+n*bx,_width);
 						break;
 					}
 				}
 				norms[by*nx+bx]=0;
 				for (int v=0;v<n;v++) {
 					for (int u=0;u<n;u++) {
 						if (v>0||u>0) {
 							switch (_norm) {
 								case L1 : {
 									norms[by*nx+bx]+=Math.abs(z[v*n+u]);
 									break;
 								}
 								case L2 : {
 									norms[by*nx+bx]+=z[v*n+u]*z[v*n+u];
 									break;
 								}
 							}
 						}
 					}
 				}
 				if (_norm==Norm.L2) {
 					norms[by*nx+bx]=Math.sqrt(norms[by*nx+bx]);
 				}
 			}
 		}
 		/*for (int by=0;by<ny;by++) {
 			String prefix="";
 			for (int bx=0;bx<nx;bx++) {
 				System.out.print(prefix+norms[by*nx+bx]);
 				prefix=",";
 			}
 			System.out.println();
 		}*/
 		return(norms);
 	}
 
 	/*public static void repaint(BufferedImage _img,int[] _luma,int _width,int _height,BlockSize _bsize,int[] _norms,int _threshold) {
 		int n=1<<_bsize.log;
 		int nx=_width/n;
 		int ny=_height/n;
 		for (int y=0;y<_height;y++) {
 			for (int x=0;x<_width;x++) {
 				int rgb=(_luma[y*_width+x]<<16)|(_luma[y*_width+x]<<8)|_luma[y*_width+x];
 				int bx=x/n;
 				int by=y/n;
 				if (bx<nx&&by<ny&&_norms[by*nx+bx]<_threshold) {
 					rgb|=0xff00;
 				}
 				_img.setRGB(x,y,rgb);
 			}
 		}
 	}*/
 
 	/**
 	 * TODO:
 	 *  x add file selector to change the image
 	 *  x radio buttons to toggle the Norm (L1, L2)
 	 *  x radio buttons to toggle the Block Size (4x4, 8x8, 16x16)
 	 *  - NumberField display for the slider (with ability to type a valid integer into it)
 	 *  x Dynamically determine slider range (0 to max(norms))
 	 *  - status bar at the bottom (mouse over shows the block coordinates)
 	 *  x call listener on image load
 	 *  - logarithm scale on slider
 	 *  - display file name in TextField
 	 */
 	public static void main(String[] _args) throws IOException {
 		JFrame sky  = new SkyDetector(_args[0]);
 		sky.setVisible(true);
 		sky.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	  
 
 };
