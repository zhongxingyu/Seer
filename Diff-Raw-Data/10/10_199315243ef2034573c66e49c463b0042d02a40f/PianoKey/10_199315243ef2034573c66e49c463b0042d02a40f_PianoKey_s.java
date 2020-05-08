 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LinearGradientPaint;
 import java.awt.MultipleGradientPaint;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JButton;
 
 import sound.MusicConstants;
 import sound.Note;
 import sound.NotePlayerChannel;
 
 public abstract class PianoKey extends JButton implements TransitionElement {
 
 	private PlayingScreen _screen;
 	private NotePlayerChannel _noteChannel;
 	private int _height;
 	private Note _note;
 	private static final long serialVersionUID = -3928144976886872593L;
 	private boolean _paintGradient = false;
 	private int _paneWidth;
 	private Color[] _colors1, _colors2;
 	private float[] _fractions1, _fractions2;
 	private int _x, _y, _defaultX, _defaultY;
 
 	public PianoKey(PlayingScreen screen, int noteValue, int height, NotePlayerChannel noteChannel){
 		_screen = screen;
 		_height = height;
 		_paneWidth = screen.getWidth();
 		_note = new Note(noteValue);
 		_noteChannel = noteChannel;
 
 		this.addMouseListener(new PianoListener());
 	}
 
 	public Note getNote() {
 		return _note;
 	}
 
 	public void setColors(Color[] colors1, Color[] colors2) {
 		_colors1 = colors1;
 		_colors2 = colors2;
 	}
 
 	public void setFractions(float[] fractions1, float[] fractions2) {
 		_fractions1 = fractions1;
 		_fractions2 = fractions2;
 	}
 
 	public void resizeKey(int numKeys, int keySpacing, int insets){
 		int spaces = numKeys * keySpacing + insets*2;
 		this.setSize((_paneWidth-spaces)/numKeys, _height);
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = ((Graphics2D) g);
 
 		Color[] colors = _colors1;
 		float[] fractions = _fractions1;
 
 		if (_paintGradient) {
 			colors = _colors2;
 			fractions = _fractions2;
 		}
 
 		LinearGradientPaint linearGradient = new LinearGradientPaint(0,0,this.getWidth()/2,0,fractions,colors,MultipleGradientPaint.CycleMethod.REFLECT);
 		g2.setPaint(linearGradient);
 		g2.fill(new Rectangle2D.Double(0,0,this.getWidth(),_height));
 	}
 
 
 	private class PianoListener implements MouseListener{
 		@Override
 		public void mouseClicked(MouseEvent e) {}
 		@Override
 		public void mousePressed(MouseEvent e) {}
 		@Override
 		public void mouseReleased(MouseEvent e) {}
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			if (_screen.isActive()) {
 				_paintGradient = true;
 				_noteChannel.noteOn(getNote().getNum(), MusicConstants.DEFAULT_VELOCITY);
 				repaint();
 			}
 		}
 		@Override
 		public void mouseExited(MouseEvent e) {
 			if (_screen.isActive()) {
 				_paintGradient = false;
 				_noteChannel.noteOff(getNote().getNum());
 				repaint();
 			}
 		}
 	}
 
 	@Override
 	public void setDefaultLocation(int x, int y) {
 		this.setLocation(x, y);
 		_x = x;
 		_y = y;
 		_defaultX = x;
 		_defaultY = y;
 	}
 
 	@Override
 	public void setLocation(int x, int y) {
 		_x = x;
 		_y = y;
 		super.setLocation(x, y);
 	}
 
 	@Override
 	public int getX() {
 		return _x;
 	}
 
 	@Override
 	public int getY() {
 		return _y;
 	}
 
 	@Override
 	public int getDefaultX() {
 		return _defaultX;
 	}
 
 	@Override
 	public int getDefaultY() {
 		return _defaultY;
 	}
 
 	@Override
 	public void setX(int newX) {
 		this.setLocation(newX, _y);
 	}
 
 	@Override
 	public void setY(int newY) {
 		this.setLocation(_x, newY);
 	}
 
 }
