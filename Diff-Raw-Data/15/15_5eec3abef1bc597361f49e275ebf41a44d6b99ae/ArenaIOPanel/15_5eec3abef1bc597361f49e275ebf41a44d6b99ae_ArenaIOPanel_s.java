 package ntu.csie.oop13spring;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 public class ArenaIOPanel extends JPanel{
 	
 	private int _no_cell_x;
 	private int _no_cell_y;
 	private int _x, _y;
 	private ArrayList<Command> _cmds;
 	
 	private ImageCell[] _imgcs;
 	private ImageCell[] _backgroundcs;
 	private int _imgcs_len, _bgcs_len, _max_imgcs;
 	
 	public ArenaIOPanel(int no_cell_x, int no_cell_y){
 		_no_cell_x = no_cell_x;
 		_no_cell_y = no_cell_y;
		_max_imgcs = 2 * _no_cell_x * _no_cell_y;
 		_imgcs = new ImageCell[_max_imgcs];
 		for(int i = 0; i < _max_imgcs; i++ )
 			_imgcs[i] = null;
 		_imgcs_len = 0;
 		
 		_backgroundcs = new ImageCell[_max_imgcs];
 		for(int i = 0; i < _max_imgcs; i++ )
 			_backgroundcs[i] = null;
 		_bgcs_len = 0;
 		
 		_x = _no_cell_x * POOConstant.CELL_X_SIZE;
 		_y = _no_cell_y * POOConstant.CELL_Y_SIZE;
 		
 		addKeyListener(new Adapter());
 		setFocusable(true);
 	}
 	
 	public int addToCell(Image img, int x, int y, int id, int paddingx, int paddingy){
 		if(id >= _max_imgcs)
 			return -1;
 		
 		ImageCell imgc = new ImageCell(img, x, y, paddingx, paddingy);
 		if(id < 0){
 			_imgcs[_imgcs_len] = imgc;
 			id = _imgcs_len;
 		}else if(id < _max_imgcs){
 			_imgcs[id] = imgc;
 		}
 		
 		if(id >= _imgcs_len){
 			_imgcs_len = id + 1;
 		}
 		return id;
 	}
 	
 	public int addToBackground(Image img, int x, int y){
 		
 		ImageCell imgc = new ImageCell(img, x, y, 0, 0);
 		if(_bgcs_len + 1 < _max_imgcs){
 			_backgroundcs[_bgcs_len] = imgc;
 			_bgcs_len++;
 		}
 		return _bgcs_len-1;
 	}
 	
 	public boolean removeFromCell(int id){
 		if(id >= _imgcs_len || id < 0)
 			return false;
 		
 		_imgcs[id] = null;
 		if(id == _imgcs_len - 1)
 			_imgcs_len--;
 		
 		return true;
 	}
 	
 	public boolean addCommandListener(ArrayList<Command> cmds){
 		_cmds = cmds;
 		return true;
 	}
 	
 	public void paint(Graphics g) {
 		
 		for(int i = 0; i < _bgcs_len; i++){
 			if(_backgroundcs[i] != null){
 				g.drawImage(_backgroundcs[i]._img, _backgroundcs[i]._x * POOConstant.CELL_X_SIZE + _backgroundcs[i]._paddingx, _backgroundcs[i]._y * POOConstant.CELL_Y_SIZE + _backgroundcs[i]._paddingy, this);
 			}
 		}
 		
 		for(int i = 0; i < _imgcs_len; i++){
 			if(_imgcs[i] != null){
 				g.drawImage(_imgcs[i]._img, _imgcs[i]._x * POOConstant.CELL_X_SIZE + _imgcs[i]._paddingx, _imgcs[i]._y * POOConstant.CELL_Y_SIZE + _imgcs[i]._paddingy, this);
 			}
 		}
 	}
 	
 	private class ImageCell{
 		public Image _img;
 		public int _x, _y, _paddingx, _paddingy;
 		
 		public ImageCell(Image img, int x, int y, int paddingx, int paddingy){
 			_img = img;
 			_x = x;
 			_y = y;
 			_paddingx = paddingx;
 			_paddingy = paddingy;
 		}
 	}
 	
 	private class Adapter extends KeyAdapter{
 		
 		public void keyPressed(KeyEvent e) {
 			if(_cmds.size() > 1)
 				return;
             int key = e.getKeyCode();
             switch(key){
             	case KeyEvent.VK_UP:
             		_cmds.add(new Command(POOConstant.Cmd.UP));
             		break;
             	case KeyEvent.VK_DOWN:
             		_cmds.add(new Command(POOConstant.Cmd.DOWN));
             		break;
             	case KeyEvent.VK_LEFT:
             		_cmds.add(new Command(POOConstant.Cmd.LEFT));
             		break;
             	case KeyEvent.VK_RIGHT:
             		_cmds.add(new Command(POOConstant.Cmd.RIGHT));
             		break;
             	case KeyEvent.VK_Z:
             		_cmds.add(new Command(POOConstant.Cmd.Z));
             		break;
             	default:;
             }
         }
 	}
 	
 }
