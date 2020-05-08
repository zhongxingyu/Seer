 package no.bouvet.kpro.gui.bar;
 
 import no.bouvet.kpro.gui.tree.AbstractNode;
import no.bouvet.kpro.gui.tree.PartNode;

import com.trolltech.qt.core.QPoint;
 import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
 import com.trolltech.qt.gui.QBrush;
 import com.trolltech.qt.gui.QColor;
 import com.trolltech.qt.gui.QGraphicsItem;
 import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QMimeData;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;
 import com.trolltech.qt.gui.QGraphicsItem.GraphicsItemChange;
 import com.trolltech.qt.gui.QGraphicsItem.GraphicsItemFlag;
 
 public class TrackItem extends QGraphicsRectItem implements Comparable<TrackItem> {
 	
 	private AbstractNode node;
 	private Track track;
 	private boolean selected = false;
 	private boolean shouldBePlayed = false;
	
 	private QColor color;
 	
 	public TrackItem(Track track, AbstractNode node) {
 		super(track);
 		this.node = node;
 		this.track = track;
 		track.addItem(this);
 		
 		color = QColor.red;
 		setVisible(false);
 		setFlags(new QGraphicsItem.GraphicsItemFlags(
 				GraphicsItemFlag.ItemIsMovable,
 				GraphicsItemFlag.ItemIsSelectable
 				));
 	}
 	
 	@Override
 	public Object itemChange(GraphicsItemChange change, Object value) {
 		switch(change) {
 		case ItemPositionChange:
 			//contain the track item inside the track
 			QPointF p = (QPointF) value;
 			p.setY(0);
 			//logic for restriction on x coordinates may be implemented when
 			//such rules are figured out (i.e. what should the boundaries be?)
 			break;
 		case ItemSelectedChange:
 			node.itemChange(change, value);
 			break;
 		}
 		
 		return super.itemChange(change, value);
 	}
 	
 	public void nodePositionChanged() {
 		track.trackitemChanged(this);
 	}
 	
 	public void nodeSelectionChanged(Boolean selected) {
 		this.selected = selected;
 		setBrush(new QBrush(selected ? color.lighter(150):color));
 	}
 	
 	public void shouldBePlayedChanged(Boolean shouldBePlayed) {
 		this.shouldBePlayed = shouldBePlayed;
 		setVisible(shouldBePlayed);
 		track.trackitemChanged(this);
 	}
 	
 	public AbstractNode getNode() {
 		return node;
 	}
 	
 	public Track getTrack() {
 		return track;
 	}
 
 	public int compareTo(TrackItem item) {
 		double thisScenePos = node.scenePos().x();
 		double itemScenePos = item.node.scenePos().x();
 		
 		if (thisScenePos > itemScenePos) {
 			return 1;
 		} else if (thisScenePos < itemScenePos) {
 			return -1;
 		} else {
 			return 0;
 		}
 	}
 	
 }
