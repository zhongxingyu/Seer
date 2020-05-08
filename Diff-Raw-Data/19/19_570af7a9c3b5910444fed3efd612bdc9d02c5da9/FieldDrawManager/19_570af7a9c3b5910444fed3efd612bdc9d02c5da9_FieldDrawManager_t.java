 package view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import model.gameoflife.Cell;
 import model.gameoflife.Pattern;
 
 /**
  *
  * @author pierre
  */
 public class FieldDrawManager {
 	
 	protected HashMap<Point, Cell> _cells;
 	protected Point _fieldSize;
 	protected int _cellSize;
 	protected Point _position;
 	protected Point _offset;
 	protected Point _indicator;
 	protected double _zoom;
 	protected Point _oldComponentSize;
 	protected Point _componentSize;
 	protected Pattern _pattern;
 	protected boolean _torus;
 	
 	public static double ZOOM_UNIT = 0.9;
 	
 
 	//private view.Cell c;
 	public FieldDrawManager(Point fieldSize) {
 
 		/*CellImageParameter p = new CellImageParameter("cell.png", new Point(10,10), 5,5,5);
 		 c = new view.Cell(new CellState(), p);
 		 */
 		_cells = new HashMap<>();
 
 		_cellSize = 1;
 		
 		_componentSize = new Point(0,0);
 
 		_position = new Point(_componentSize.x / 2, _componentSize.y / 2);
 
 		_offset = new Point(0, 0);
 
 		_fieldSize = new Point(1, 1);
 
 		_indicator = null;
 
 		_zoom = 1;
 
 		_oldComponentSize = new Point(_componentSize.x, _componentSize.y);
 		
 		_pattern = null;
 		
 		_torus = false;
 		
 		//_exec = true;
 
 	}
 
 	FieldDrawManager(FieldDrawManager fdm) {
 		_cells = fdm._cells;
 		_fieldSize = fdm._fieldSize;
 		_cellSize = fdm._cellSize;
 		_position = fdm._position;
 		_offset = fdm._offset;
 		_indicator = fdm._indicator;
 		_zoom = fdm._zoom;
 		_oldComponentSize = fdm._oldComponentSize;
 		_componentSize = fdm._componentSize;
 		_pattern = fdm._pattern;
 		_torus = fdm._torus;
 	}
 
 	protected synchronized void drawBorder(Graphics g) {
 		g.drawRect(
 			_offset.x,
 			_offset.y,
 			_fieldSize.x * (int) (_cellSize * _zoom),
 			_fieldSize.y * (int) (_cellSize * _zoom));
 	}
 	
 	protected synchronized void drawIndicator(Graphics g) {
 		g.drawRect(
 			(int) (_cellSize * _zoom) * _indicator.x + _offset.x,
 			(int) (_cellSize * _zoom) * _indicator.y + _offset.y,
 			/*(int) ((_cellSize * _indicator.x + _offset.x) * _zoom),
 			 (int) ((_cellSize * _indicator.y + _offset.y) * _zoom),*/
 			(int) (_cellSize * _zoom),
 			(int) (_cellSize * _zoom));
 	}
 	
 	protected synchronized void drawPattern(Graphics g) {
 		Iterator<Point> it = _pattern.getCellsByMiddle().iterator();
 		while (it.hasNext()) {
			Point t = it.next();
			Point temp = (Point) t.clone();
 			if(isInsideTheField(new Point(_indicator.x + temp.x, _indicator.y + temp.y))){
 					this.drawPoint(g, temp);
 			}
 			else{
 				if(this.isTorus()){
 					temp = calculateTorus(temp);
 					this.drawPoint(g, temp);
 				}
 			}
 		}
 	}
 	
 	protected synchronized void drawPoint(Graphics g, Point p){
 		g.fillOval(
 				(int) (_cellSize * _zoom) * (_indicator.x + p.x) + _offset.x,
 				(int) (_cellSize * _zoom) * (_indicator.y + p.y) + _offset.y,
 				/*(int) ((_cellSize * _indicator.x + _offset.x) * _zoom),
 				 (int) ((_cellSize * _indicator.y + _offset.y) * _zoom),*/
 				(int) (_cellSize * _zoom),
 				(int) (_cellSize * _zoom));
 	}
 	
 	protected synchronized Point calculateTorus(Point p){
 		if((_indicator.x + p.x) >= _fieldSize.x){
 			p.x = (_indicator.x + p.x) % _fieldSize.x - _indicator.x;
 		}
 		else{
 			while((_indicator.x + p.x) < 0){
 				p.x = p.x + _fieldSize.x;
 			}
 		}
 		if((_indicator.y + p.y) >= _fieldSize.y){
 			p.y = (_indicator.y + p.y) % _fieldSize.y - _indicator.y;					
 		}
 		else{
 			while((_indicator.y + p.y) < 0){
 				p.y = p.y + _fieldSize.y;
 			}
 		}
 		return p;
 	}
 	
 	protected synchronized void drawCells(Graphics g, Point p1, Point p2) {
 		
 		for (Map.Entry<Point, Cell> entry : _cells.entrySet()) {
 			Point coord = entry.getKey();
 			if (coord.x >= p1.x
 				&& coord.y >= p1.y
 				&& coord.x <= p2.x
 				&& coord.y <= p2.y) {
 				
 				Point position = new Point(_position);
 				position.x = (int) (_cellSize * _zoom) * coord.x + _offset.x;
 				position.y = (int) (_cellSize * _zoom) * coord.y + _offset.y;
 				
 				g.fillOval(
 						position.x,
 						position.y,
 						(int) (_cellSize * _zoom),
 						(int) (_cellSize * _zoom));
 			}
 		}
 	}
 	
 	public synchronized void draw(Graphics g) {
 
 		g.setColor(Color.BLACK);
 		//g.setColor(new Color(0, 0, 0, 1));
 		g.fillRect(0, 0, _componentSize.x, _componentSize.y);
 
 		//TODO: Mettre ce calcul autre part
 		_offset.x = _position.x - (int) (_fieldSize.x * _cellSize * _zoom) / 2;
 		_offset.y = _position.y - (int) (_fieldSize.y * _cellSize * _zoom) / 2;
 
 
 		g.setColor(Color.DARK_GRAY);
 		this.drawBorder(g);
 
 		Point p1 = new Point();
 		Point p2 = new Point();
 
 		p1.x = (int) (-_offset.x / _zoom) / _cellSize;
 		p1.y = (int) (-_offset.y / _zoom) / _cellSize;
 		p2.x = (int) ((_componentSize.x - _offset.x) / (int)(_cellSize * _zoom));
 		p2.y = (int) ((_componentSize.y - _offset.y) / (int)(_cellSize * _zoom));
 		
 		g.setColor(Color.red);
 		this.drawCells(g, p1, p2);
 		
 		
 		if (_indicator != null) {
 			if(_pattern == null) {
 				g.setColor(Color.LIGHT_GRAY);
 				this.drawIndicator(g);
 			}
 			else {
 				g.setColor(new Color(.3f, .4f, .5f, .6f));
 				this.drawPattern(g);
 			}
 			
 			g.setColor(new Color(200, 200, 200, 200));
 			g.setFont(new Font("Tahoma",Font.PLAIN,11));
 			String info = "x:" + _indicator.x + " \t y:" + _indicator.y;
 			g.drawString(info, _componentSize.x - info.length() * 6, _componentSize.y - 10);
 		}
 	}
 
 	/*public synchronized void update(Observable o, Object o1) {
 
 		if (o instanceof GameExecution) {
 			GameExecution game = (GameExecution) o;
 			
 			_cells = game.getCells();
 			_size = game.getFieldSize();
 
 			this.repaint();
 		}
 	}*/
 
 	/*public synchronized void setNeighbors(int n) {
 		_cell = new view.FieldDrawManager();
 	}*/
 	
 	/**
 	 * Retourne la position de la cellule a partir d'une position dans le composant.
 	 * @param coord La position dans le composant.
 	 * @return La coordonnée de la céllule
 	 */
 	public Point cellCoordinate(Point coord) {
 		Point cell = new Point();
 		if(coord.x < _offset.x){
 			cell.x = -1;
 		}
 		else{
 			cell.x = (coord.x - _offset.x) / (int) (_cellSize * _zoom);
 		}
 		if(coord.y < _offset.y){
 			cell.y = -1;
 		}
 		else{
 			cell.y = (coord.y - _offset.y) / (int) (_cellSize * _zoom);
 		}
 		return cell;
 	}
 
 	/**
 	 * Change la position de l'indicateur en fonction des coordonées dans le composant.
 	 * @param coord Coordonnées dans le composant.
 	 * @return True si il faut repaindre le composant, false sinon.
 	 */
 	public boolean setIndicatorPosition(Point coord) {
 		boolean repaint = false;
 		if(coord == null){
 			_indicator = null;
 			repaint = true;
 		}
 		else{
 			coord = this.cellCoordinate(coord);
 			if (isInsideTheField(coord)) {
 				if (!coord.equals(_indicator)) {
 					_indicator = coord;
 
 					repaint = true;
 				}
 			} else {
 				_indicator = null;
 			}
 		}
 		return repaint;
 	}
 
 	public boolean isInsideTheField(Point coord) {
 		return coord.x >= 0 && coord.x < _fieldSize.x
 			&& coord.y >= 0 && coord.y < _fieldSize.y;
 	}
 
 	public void moveField(Point movement) {
 		/*_offset.x += movement.x;
 		 _offset.y += movement.y;*/
 		_position.x += movement.x;
 		_position.y += movement.y;
 		_offset.x = _position.x - (int) (_fieldSize.x * _cellSize * _zoom) / 2;
 		_offset.y = _position.y - (int) (_fieldSize.y * _cellSize * _zoom) / 2;
 
 		//this.repaint();
 	}
 
 	public void zoom(int unit) {
 		_zoom *= Math.pow(Field.ZOOM_UNIT, unit);
 		//System.out.println(_zoom);
 
 		if (_zoom > 1) {
 			_zoom = 1;
 		} else if (_zoom < 0.2) {
 			_zoom = 0.2;
 		}
 
 		//_drawer.setZoom(_zoom);
 		
 		//this.repaint();
 	}
 
 	public Point getIndicator() {
 		return _indicator;
 	}
 
 	public void resize() {
 		Point size = new Point();
 		size.x = _componentSize.x - _oldComponentSize.x;
 		size.y = _componentSize.y - _oldComponentSize.y;
 
 		_position.x += size.x / 2;
 		_position.y += size.y / 2;
 
 		_oldComponentSize.x = _componentSize.x;
 		_oldComponentSize.y = _componentSize.y;
 	}
 
 	/*public HashMap<Point, Cell> getCells() {
 		return _cells;
 	}*/
 
 	public void setCells(HashMap<Point, Cell> cells) {
 		this._cells = cells;
 	}
 
 	/*public Point getFieldSize() {
 		return _fieldSize;
 	}*/
 
 	public void setFieldSize(Point fieldSize) {
 		this._fieldSize = fieldSize;
 	}
 
 	public int getCellSize() {
 		return _cellSize;
 	}
 
 	public void setCellSize(int cellSize) {
 		this._cellSize = cellSize;
 	}
 
 	public Point getPosition() {
 		return _position;
 	}
 
 	public void setPosition(Point position) {
 		this._position = position;
 	}
 
 	/*public Point getOffset() {
 		return _offset;
 	}
 
 	public void setOffset(Point offset) {
 		this._offset = offset;
 	}*/
 
 	public double getZoom() {
 		return _zoom;
 	}
 
 	public void setZoom(double zoom) {
 		this._zoom = zoom;
 	}
 
 	/*public Point getOldComponentSize() {
 		return _oldComponentSize;
 	}
 
 	public void setOldComponentSize(Point oldComponentSize) {
 		this._oldComponentSize = oldComponentSize;
 	}*/
 
 	/*public Point getComponentSize() {
 		return _componentSize;
 		* }*/
 
 	public void setComponentSize(Point componentSize) {
 		this._componentSize = componentSize;
 	}
 
 	public Pattern getPattern() {
 		return _pattern;
 	}
 
 	public void setPattern(Pattern _pattern) {
 		this._pattern = _pattern;
 		
 	}
 	
 	public void verticalSymmetry(){
 		if(_pattern != null){
 			_pattern.verticalSymmetry();
 		}
 	}
 	
 	public void horizontalSymmetry(){
 		if(_pattern != null){
 			_pattern.horizontalSymmetry();
 		}
 	}
 	
 	public void rotateRight(){
 		if(_pattern != null){
 			_pattern.rotateRight();
 		}
 	}
 	
 	public void rotateLeft(){
 		if(_pattern != null){
 			_pattern.rotateLeft();
 		}
 	}
 
 	public boolean isTorus() {
 		return _torus;
 	}
 
 	public void setTorus(boolean _torus) {
 		this._torus = _torus;
 	}
 
 	boolean isPatternDefine() {
 		return (_pattern != null)?true:false;
 	}
 	
 }
