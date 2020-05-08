 package client;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.trolltech.qt.core.QPoint;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.core.Qt.MouseButton;
 import com.trolltech.qt.gui.*;
 
 import data.Card;
 import data.GameData;
 
 public class BoardView extends QGraphicsView
 	{
 	public List<AnimatedCard> cards=new LinkedList<AnimatedCard>();
 	
 	public double zoom=0.3;
 
 	public GameData gameData=new GameData(); 
 
 	public BoardView(QWidget parent)
 		{
 		super(parent);
 
 		
 		
 		for(int i=1;i<=10;i++)
 			{
 			Card cdata=new Card();
 			cdata.front="spades "+i;
 			cdata.back="poker back";
 			
 			AnimatedCard c=new AnimatedCard(cdata);
 			//c.loadImageFront("cards/spades"+i+".png");
 			//c.loadImage("cards/spades"+i+".svg");
 			c.posX=40+i*20;
 			c.posY=40+i*20;
 			c.posZ=10-i;
 			cards.add(c);
 			}
 	
 		
 		redoLayout();
 		
     setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
     setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
 		setFixedSize(sizeHint());
 
 		setMouseTracking(true);
 		setEnabled(true);
 		
 		
 		
 		
 		}
 	
 	protected void mouseDoubleClickEvent(QMouseEvent event)
 		{
 		System.out.println("here!");
 		}
 	
 	
 	QPoint oldMousePos=new QPoint();
 	@Override
 	protected void mouseMoveEvent(QMouseEvent event)
 		{		
 		boolean needRedraw=false;
 		QPoint dx=event.pos().subtract(oldMousePos);
 		for(AnimatedCard card:cards)
 			if(card.isBeingDragged) //A separate list of dragged cards would make this faster and scale better!
 				{
 
 				//card.posX+=1;
 
 				card.posX+=dx.x()/zoom;
 				card.posY+=dx.y()/zoom;
 
 //				card.posX+=dx.x()*zoom;
 	//			card.posY+=dx.y()*zoom;
 				needRedraw=true;
 				}
 		
 		if(needRedraw)
 			{
 			redoLayout();
 			}
 		
 		oldMousePos=event.pos();
 
 		}
 	
 	protected void mousePressEvent(QMouseEvent event)
 		{
 		if(event.button()==MouseButton.LeftButton)
 			{
 			
 			
 			QGraphicsItemInterface picked=scene().itemAt(event.posF());
			if(picked!=null)
 				{
				for(AnimatedCard card:cards)
 					{
					if(card.imageFront==picked || card.imageBack==picked)
					//if(card.image.contains(pos))
						{
//						System.out.println("drag");
						card.isBeingDragged=true;
						//TODO sometimes there are cards on top to move as well
						
						}
 					}
 				}
 			
			
 
 			/*
 			System.out.println("hello");
 			
 			for(Card card:cards)
 				{
 				QPointF pos=event.posF();
 				
 				if(card.image.contains(pos))
 					{
 //					System.out.println("drag");
 					card.isBeingDragged=true;
 					//TODO sometimes there are cards on top to move as well
 					
 					}
 				}*/
 			}
 			
 		}
 	
 	@Override
 	protected void mouseReleaseEvent(QMouseEvent event)
 		{
 		if(event.button()==MouseButton.LeftButton)
 			for(AnimatedCard card:cards)
 				card.isBeingDragged=false;
 		redoLayout();
 		}
 	
 	@Override
 	protected void moveEvent(QMoveEvent event)
 		{
 		
 		}
 
 	
 	public void redoLayout()
 		{
 
 		QGraphicsScene s=new QGraphicsScene();
 		setScene(s);
 		//s.setSceneRect(0, 0, width(), height());
 		s.setSceneRect(0, 0, 400,400);
 		
 		//Sort the cards in Z to ensure the right drawing order
 		Collections.sort(cards, new Comparator<AnimatedCard>()
 			{
 			public int compare(AnimatedCard o1, AnimatedCard o2)
 				{
 				//If cards are being dragged then they should be above all other cards.
 				//But dragged cards in turn have an order
 				int o1level=o1.posZ;
 				int o2level=o2.posZ;
 				if(o1.isBeingDragged)
 					o1level+=1000;
 				if(o2.isBeingDragged)
 					o2level+=1000;				
 				
 				if(o1level<o2level)
 					return -1;
 				else if(o1level>o2level)
 					return 1;
 				else
 					return 0;
 				}
 			});
 		
 		//Render cards, in order
 		int curz=0;
 		for(AnimatedCard card:cards)
 			{
 			QGraphicsItemInterface cardImage;
 			
 			if(card.cardData.showsFront)
 				{
 				if(card.imageFront==null)
 					card.imageFront=gameData.getImageForCard(card.cardData.front);
 				cardImage=card.imageFront;
 				}
 			else
 				{
 				if(card.imageBack==null)
 					card.imageBack=gameData.getImageForCard(card.cardData.back);
 				cardImage=card.imageBack;
 				}
 				
 			
 			
 			cardImage.setZValue(curz);
 			cardImage.resetTransform();
 			//card.image.setPos(card.posX*zoom, card.posY*zoom);
 			cardImage.setTransform(QTransform.fromScale(zoom, zoom), true);
 			cardImage.setTransform(QTransform.fromTranslate(card.posX, card.posY), true);
 
 			
 			//TODO some way of finding the object?
 			
 			s.addItem(cardImage);
 
 			curz++;
 			}
 		
 		//repaint();
 //		show();
 		}
 	
 	
 	//protected void drawForeground(QPainter painter, QRectF rect) 
 	//protected void paintEvent(QPaintEvent event) //{};
 	//protected void drawBackground(QPainter painter, QRectF rect)
 	/*
 		{
 		System.out.println("here");
 		//painter.save();
 		//painter.resetTransform();
 		
 		painter.setBrush(new QBrush(QColor.blue));
 		painter.drawLine(0, 0, 30,30);
 		
 //		QGraphicsScene s=new QGraphicsScene();
 
 
 		
 		//Sort the cards in Z to ensure the right drawing order
 		Collections.sort(cards, new Comparator<Card>()
 			{
 			public int compare(Card o1, Card o2)
 				{
 				//If cards are being dragged then they should be above all other cards.
 				//But dragged cards in turn have an order
 				int o1level=o1.posZ;
 				int o2level=o2.posZ;
 				if(o1.isBeingDragged)
 					o1level+=1000;
 				if(o2.isBeingDragged)
 					o2level+=1000;				
 				
 				if(o1level<o2level)
 					return -1;
 				else if(o1level>o2level)
 					return 1;
 				else
 					return 0;
 				}
 			});
 		
 		//Render cards, in order
 		for(Card card:cards)
 			{
 			//card.image.resetTransform();
 			//card.image.setTransform(QTransform.fromScale(zoom, zoom), false);
 //			card.image.setPos(card.posX*zoom, card.posY*zoom);
 
 			
 		//	card.image.paint(painter, null, this);
 		
 			//TODO some way of finding the object?
 			
 			//s.addItem(card.image);
 
 			}
 		
 		}*/
 	
 	}
