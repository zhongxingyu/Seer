 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.editor.gui.elementpanels.conversation.representation;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.conversation.node.ConversationNodeView;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.config.ConversationConfigData;
 import es.eucm.eadventure.editor.control.controllers.conversation.GraphConversationDataControl;
 import es.eucm.eadventure.editor.gui.elementpanels.conversation.representation.graphicnode.DialogueGraphicNode;
 import es.eucm.eadventure.editor.gui.elementpanels.conversation.representation.graphicnode.GraphicNode;
 import es.eucm.eadventure.editor.gui.elementpanels.conversation.representation.graphicnode.InitialGraphicNode;
 import es.eucm.eadventure.editor.gui.elementpanels.conversation.representation.graphicnode.OptionGraphicNode;
 
 /**
  * This interface contains the methods to draw a conversation into a panel and
  * calculate its size
  */
 public class GraphicRepresentation {
 
     /**
      * Size of the body of the arrow.
      */
     private static final int ARROW_SIZE = 15;
 
     /**
      * Margins to paint the conversation
      */
    private static final int CONVERSATION_MARGIN = 150;
 
     /**
      * Normal state of the representation
      */
     private static final int NORMAL = 0;
 
     /**
      * State of the representation which a node is being moved
      */
     private static final int MOUSE_DRAG = 1;
 
     /**
      * State of the representation
      */
     private int state = NORMAL;
 
     /* Attributes */
 
     /**
      * Graph conversation data control.
      */
     private GraphConversationDataControl graphConversationDataControl;
 
     /**
      * Size of the represented conversation
      */
     private Dimension conversationSize;
 
     /**
      * Position of the upper left corner of the conversation
      */
     private Point upperLeftCorner;
 
     /**
      * Last point for the drag&drop operation of nodes
      */
     private Point lastPoint;
 
     /**
      * Node which is being moved (by dragging the mouse)
      */
     private GraphicNode pickedNode;
 
     /**
      * Index of pickedNode
      */
     private int pickedNodeIndex;
 
     protected float scale = 1.0f;
 
     /**
      * Set of graphic nodes representating the conversation
      */
     protected List<GraphicNode> graphicNodes;
 
     protected GraphicNode selectedNode;
 
     protected GraphicNode selectedChildNode;
 
     /**
      * Constructor.
      * 
      * @param graphConversationDataControl
      *            Conversation data control
      * @param containerSize
      *            Size of the container of the representation
      */
     public GraphicRepresentation( GraphConversationDataControl graphConversationDataControl, Dimension containerSize ) {
 
         this.graphConversationDataControl = graphConversationDataControl;
         graphicNodes = new ArrayList<GraphicNode>( );
 
         /* If the position of the nodes have been previously set, that will be the initial drawing of the conversation. Otherwise, the initial drawing of the conversation displays the nodes in a circumference */
         boolean posConfigured = ConversationConfigData.isConversationConfig( graphConversationDataControl.getId( ) );
         String convId = graphConversationDataControl.getId( );
 
         // Extract all the nodes in the conversation 
         List<ConversationNodeView> nodes = graphConversationDataControl.getAllNodesViews( );
 
         // Calculate the radius and the center of the circumference
         double radius = ( nodes.size( ) * GraphicNode.NODE_DIAMETER * 2 ) / ( 2 * Math.PI );
         radius = Math.max( 50, radius );
         int centerX = 50 + (int) radius;
         int centerY = 150 + (int) radius;
 
         // Create the initial graphic node (always the first in the vector)
         if( posConfigured ) {
             centerX = ConversationConfigData.getNodeX( convId, 0 );
             centerY = ConversationConfigData.getNodeY( convId, 0 );
         }
         else {
             ConversationConfigData.setNodeX( convId, 0, centerX );
             ConversationConfigData.setNodeY( convId, 0, centerY );
         }
         graphicNodes.add( new InitialGraphicNode( nodes.get( 0 ), new Point( centerX, centerY ) ) );
 
         // Set the initial angle to 90 (upper position)
         double angle = ( Math.PI / 2 );
         // For each node (except for the first one)
         for( int i = 1; i < nodes.size( ); i++ ) {
             ConversationNodeView node = nodes.get( i );
 
             // Calculate the angle in which the node will be placed in the circumference
             // This angle swings between one side and another of the circumference in each iteration
             angle += ( ( ( 2 * Math.PI ) / ( nodes.size( ) - 1 ) ) * ( i - 1 ) * Math.pow( -1, i - 1 ) );
 
             // Calculate the position of the current node
             int pointX = centerX + (int) ( Math.cos( angle ) * radius );
             int pointY = centerY - (int) ( Math.sin( angle ) * radius );
             if( posConfigured ) {
                 pointX = ConversationConfigData.getNodeX( convId, i );
                 pointY = ConversationConfigData.getNodeY( convId, i );
             }
             else {
                 ConversationConfigData.setNodeX( convId, i, pointX );
                 ConversationConfigData.setNodeY( convId, i, pointY );
             }
             Point nodePosition = new Point( pointX, pointY );
 
             if( node.getType( ) == ConversationNodeView.DIALOGUE )
                 graphicNodes.add( new DialogueGraphicNode( node, nodePosition ) );
             else if( node.getType( ) == ConversationNodeView.OPTION )
                 graphicNodes.add( new OptionGraphicNode( node, nodePosition ) );
         }
 
         updateLinksBetweenNodes( );
 
         updateConversationSize( );
 
         if( !posConfigured )
             setContainerSize( containerSize );
     }
 
     /**
      * Returns the size of the painted conversation.
      * 
      * @return The dimension of the conversation
      */
     public Dimension getConversationSize( ) {
 
         return conversationSize;
     }
 
     /**
      * Returns reference to the node in the given position.
      * 
      * @param point
      *            Position of the possible node
      * @return Reference to node if present, null otherwise
      */
     public ConversationNodeView getNodeInPosition( Point point ) {
 
         ConversationNodeView node = null;
 
         for( int i = 0; i < graphicNodes.size( ) && node == null; i++ )
             if( graphicNodes.get( i ).isInside( scale, point ) )
                 node = graphicNodes.get( i ).getNode( );
 
         return node;
     }
 
     /**
      * Returns the position of the given conversational node, null if the node
      * is not present.
      * 
      * @param node
      *            Node which position we want to know
      * @return The point in which the given node is placed
      */
     public Point getPositionOfNode( ConversationNodeView node ) {
 
         Point point = null;
 
         // For each graphic node, and while the selected nde has not been found
         for( int i = 0; i < graphicNodes.size( ) && point == null; i++ )
             // If the current graphic node holds the node we are searching, store the position of the node
             if( graphicNodes.get( i ).getNode( ) == node )
                 point = graphicNodes.get( i ).getPosition( scale );
 
         return point;
     }
 
     /**
      * Sets the size of the container component (usually a JPanel).
      * 
      * @param containerSize
      *            New size for the container
      */
     public void setContainerSize( Dimension containerSize ) {
 
         // Set the left start position of the conversation
         int positionX;
         // If the container is smaller than the conversation, the position is 0
         if( containerSize.getWidth( ) < conversationSize.getWidth( ) )
             positionX = 0;
         // If the container is bigger than the conversation, calculate position to center the conversation
         else
             positionX = containerSize.width / 2 - conversationSize.width / 2;
 
         // Set the up start position of the conversation
         int positionY;
         // If the container is smaller than the conversation, the position is 0
         if( containerSize.getHeight( ) < conversationSize.getHeight( ) )
             positionY = 0;
         // If the container is bigger than the conversation, calculate position to center the conversation
         else
             positionY = containerSize.height / 2 - conversationSize.height / 2;
 
         // Calculate the difference between the current position and the new position
         Point difference = new Point( positionX - upperLeftCorner.x, positionY - upperLeftCorner.y );
 
         // If there is a real difference
         if( difference.x != 0 || difference.y != 0 )
             // Move all nodes
             for( int i = 0; i < graphicNodes.size( ); i++ ) {
                 graphicNodes.get( i ).moveNode( difference );
 
                 // Update the config data of the conversation
                 String id = this.graphConversationDataControl.getId( );
                 ConversationConfigData.setNodePos( id, i, graphicNodes.get( i ).getPosition( 1.0f ) );
             }
 
         // Set the new upper left corner
         upperLeftCorner.x += difference.getX( );
         upperLeftCorner.y += difference.getY( );
     }
 
     /**
      * Draw the conversation in the given Graphics.
      * 
      * @param g
      *            Graphics to perform draw
      */
     public void drawConversation( Graphics g ) {
 
         g.setColor( Color.BLACK );
 
         // For each graphic node
         for( int i = 0; i < graphicNodes.size( ); i++ ) {
             GraphicNode currentNode = graphicNodes.get( i );
             g.setColor( Color.BLACK );
 
             // For each child, draw a line from the current node to the child node
             for( int j = 0; j < currentNode.getChildCount( ); j++ ) {
 
                 Point childPosition = currentNode.getChildPosition( scale, j );
 
                 if( graphicNodes.get( i ) == this.selectedNode ) {
                     g.setColor( Controller.generateColor( j ) );
                     if( currentNode instanceof OptionGraphicNode ) {
                         String text = currentNode.getNode( ).getLineText( j );
                         double nodeX = currentNode.getPosition( scale ).getX( );
                         double nodeY = currentNode.getPosition( scale ).getY( );
                         double childX = childPosition.getX( );
                         double childY = childPosition.getY( );
                         double h = Math.sqrt( Math.pow( nodeX - childX, 2 ) + Math.pow( nodeY - childY, 2 ) );
 
                         double posX = nodeX - 80 * ( nodeX - childX ) * scale / h;
                         double posY = nodeY - 80 * ( nodeY - childY ) * scale / h;
 
                         if( text.length( ) > 15 )
                             text = text.substring( 0, 12 ) + "...";
                         g.drawString( text, (int) posX, (int) posY );
                     }
                 }
                 g.drawLine( (int) currentNode.getPosition( scale ).getX( ), (int) currentNode.getPosition( scale ).getY( ), (int) childPosition.getX( ), (int) childPosition.getY( ) );
                 drawArrow( g, currentNode.getChildNode( j ), currentNode.getPosition( scale ), childPosition );
             }
         }
 
         // For each graphic node
         for( int i = 0; i < graphicNodes.size( ); i++ ) {
             GraphicNode currentNode = graphicNodes.get( i );
             // Paint the node
             currentNode.drawNode( scale, g );
         }
     }
 
     /**
      * Updates the graphic representation of the conversation, when a node has
      * been added or removed, or when the container has been resized.
      */
     public void update( ) {
 
         // Pick all the nodes from the conversation
         List<ConversationNodeView> nodes = graphConversationDataControl.getAllNodesViews( );
 
         // New vector to store the nodes that are not currently represented (the new nodes)
         List<ConversationNodeView> newReducedConversationNodes = new ArrayList<ConversationNodeView>( );
 
         // New vector to store the update graphic nodes
         List<GraphicNode> newGraphicNodes = new ArrayList<GraphicNode>( );
 
         // Insert as null pointers as nodes has the conversation (it's important that the new graphic node vector has a
         // stable size)
         for( int i = 0; i < nodes.size( ); i++ )
             newGraphicNodes.add( null );
 
         // For each node in the conversation
         for( int i = 0; i < nodes.size( ); i++ ) {
             ConversationNodeView node = nodes.get( i );
 
             // Search for the graphic node representing the current node
             GraphicNode graphicNode = null;
             for( int j = 0; j < graphicNodes.size( ) && graphicNode == null; j++ ) {
                 // If the current graphic node holds the conversational node, store it
                 if( graphicNodes.get( j ).getNode( ) == node )
                     graphicNode = graphicNodes.get( j );
             }
 
             // If the graphic node has been found, store it into the new vector of graphic nodes
             if( graphicNode != null ) {
                 // Pick the position of the conversational node in its vector
                 int nodePosition = nodes.indexOf( node );
 
                 // Set the graphic node in the same position of the conversational node
                 newGraphicNodes.set( nodePosition, graphicNode );
             }
 
             // If the graphic node hasn't been found, it means that the node is new in the representation
             else
                 newReducedConversationNodes.add( node );
         }
 
         // If there are conversational nodes without graphic nodes
         if( newReducedConversationNodes.size( ) > 0 ) {
             // For each new conversational node
             for( int i = 0; i < newReducedConversationNodes.size( ); i++ ) {
                 // Extract the node and the index in the vector
                 ConversationNodeView node = newReducedConversationNodes.get( i );
                 int nodeIndex = nodes.indexOf( node );
 
                 // Search the position of its oldest brother (the one on the right), and move it 100 to the right
                 Point nodePosition = getPositionOfOldestBrother( node );
                 nodePosition.x += 100;
                 // Search the position of its father, and move it 100 units down
                 if( nodePosition.x == Integer.MIN_VALUE || nodePosition.y == Integer.MIN_VALUE ) {
                     nodePosition = getPositionOfFather( node );
                     nodePosition.y += 100;
                 }
 
                 // Create a new graphic node, with the node and the position, and set it into the vector (in the same
                 // position as node)
                 ConversationConfigData.setNodePos( graphConversationDataControl.getId( ), nodeIndex, nodePosition );
                 // If it is a dialogue node, create a dialogue graphic node
                 if( node.getType( ) == ConversationNodeView.DIALOGUE )
                     newGraphicNodes.set( nodeIndex, new DialogueGraphicNode( node, nodePosition ) );
 
                 // If it is an option node, create an option graphic node
                 else
                     newGraphicNodes.set( nodeIndex, new OptionGraphicNode( node, nodePosition ) );
             }
         }
 
         // Set the new vector of graphic nodes, and update the links
         graphicNodes = newGraphicNodes;
         updateLinksBetweenNodes( );
 
         // Update the conversation size
         updateConversationSize( );
     }
 
     /**
      * Returns the position of the father of the given node
      * 
      * @param node
      *            Node which father we want to know the position
      */
     private Point getPositionOfFather( ConversationNodeView node ) {
 
         ConversationNodeView father = null;
 
         // Pick all the nodes in the conversation
         List<ConversationNodeView> nodes = graphConversationDataControl.getAllNodesViews( );
 
         // For each node in the conversation
         for( int i = 0; i < nodes.size( ) && father == null; i++ ) {
             ConversationNodeView possibleFather = nodes.get( i );
 
             // Check for all the children nodes of the current node (possible father)
             for( int j = 0; j < possibleFather.getChildCount( ); j++ )
                 // If the child is the same as the given node, we have found the father
                 if( possibleFather.getChildView( j ) == node )
                     father = possibleFather;
         }
 
         // Return a copy of the position of the father
         return new Point( graphicNodes.get( nodes.indexOf( father ) ).getPosition( scale ) );
     }
 
     /**
      * Returns the position of the brother of the given node which is on the
      * right. If no brother is found, or if the given node has no parent,
      * (-INF,-INF) is returned
      * 
      * @param node
      *            Node
      */
     private Point getPositionOfOldestBrother( ConversationNodeView node ) {
 
         ConversationNodeView father = null;
 
         // Pick all the nodes in the conversation
         List<ConversationNodeView> nodes = graphConversationDataControl.getAllNodesViews( );
 
         // For each node in the conversation
         for( int i = 0; i < nodes.size( ) && father == null; i++ ) {
             ConversationNodeView possibleFather = nodes.get( i );
 
             // Check for all the children nodes of the current node (possible father)
             for( int j = 0; j < possibleFather.getChildCount( ); j++ )
                 // If the child is the same as the given node, we have found the father
                 if( possibleFather.getChildView( j ) == node ) {
                     father = possibleFather;
                 }
         }
 
         // Take the position of the brother on the right
         Point maxPoint = new Point( Integer.MIN_VALUE, Integer.MIN_VALUE );
         if( father != null ) {
             for( int j = 0; j < father.getChildCount( ); j++ ) {
                 if( father.getChildView( j ) != node ) {
                     ConversationNodeView possibleBrother = father.getChildView( j );
                     if( graphicNodes.get( nodes.indexOf( possibleBrother ) ).getPosition( scale ).x > maxPoint.x ) {
                         maxPoint = graphicNodes.get( nodes.indexOf( possibleBrother ) ).getPosition( scale );
                     }
                 }
             }
         }
 
         // Return a copy of the position of the father
         return (Point) maxPoint.clone( );
     }
 
     /**
      * Notifies the graphic representation that a mouse button has been pressed
      * (not clicked, but pressed and held).
      * 
      * @param point
      *            Point in which the mouse has been pressed
      */
     public void mousePressed( Point point ) {
 
         pickedNode = null;
 
         // For each graphic node, and while the clicked node has not been found
         for( int i = ( graphicNodes.size( ) - 1 ); i >= 0 && pickedNode == null; i-- )
             // If the current graphic node has been clicked (25 points of radius), store the node
             if( graphicNodes.get( i ).isInside( scale, point ) ) {
                 pickedNode = graphicNodes.get( i );
                 pickedNodeIndex = i;
             }
 
         // If a valid node has been picked and the state is normal
         if( pickedNode != null && state == NORMAL ) {
             // Pick the initial point and change the state
             lastPoint = point;
             state = MOUSE_DRAG;
         }
     }
 
     /**
      * Notifies the graphic representation that the mouse has been dragged.
      * 
      * @param point
      *            Point in which the mouse has been dragged
      * @return True if changes have been made, to repaint the panel, false
      *         otherwise
      */
     public boolean mouseDragged( Point point ) {
 
         boolean modified = false;
 
         // If te mouse is being dragged
         if( state == MOUSE_DRAG ) {
             // Calculate the difference between the last position and the new position
             Point moved = new Point( (int) ( ( point.x - lastPoint.x ) / scale ), (int) ( ( point.y - lastPoint.y ) / scale ) );
 
             // Move the picked node
             pickedNode.moveNode( moved );
 
             // Update the config data of the conversation
             String id = this.graphConversationDataControl.getId( );
             ConversationConfigData.setNodePos( id, pickedNodeIndex, pickedNode.getPosition( 1.0f ) );
 
             // Set the point to the new node and set modified to true
             lastPoint = point;
             modified = true;
         }
 
         return modified;
     }
 
     /**
      * Notifies the graphic representation that the mouse button has been
      * released.
      * 
      * @return True if changes have been made, to repaint and resize the panel,
      *         false otherwise
      */
     public boolean mouseReleased( ) {
 
         boolean modified = false;
 
         // If the mouse is being dragged
         if( state == MOUSE_DRAG ) {
             // Update the conversation size
             updateConversationSize( );
 
             // Turn state to normal and set modified to true
             state = NORMAL;
             modified = true;
         }
 
         return modified;
     }
 
     /**
      * Draws a little arrow head.
      * 
      * @param g
      *            Graphics to perform draw
      * @param startPoint
      *            Start point of the line
      * @param endPoint
      *            End point of the line
      */
     protected void drawArrow( Graphics g, GraphicNode node, Point startPoint, Point endPoint ) {
 
         double dX, dY, angle = 0;
 
         dX = startPoint.getX( ) - endPoint.getX( );
         dY = startPoint.getY( ) - endPoint.getY( );
 
         if( dX == 0 && dY > 0 )
             angle = Math.PI / 2;
         if( dX == 0 && dY < 0 )
             angle = 3 * Math.PI / 2;
         if( dX > 0 )
             angle = Math.atan( dY / dX );
         if( dX < 0 )
             angle = Math.atan( dY / dX ) + Math.PI;
 
         int[] pointsX = new int[ 3 ];
         int[] pointsY = new int[ 3 ];
 
         // Obtain the first point of the arrow
         boolean found = false;
         int i = 0;
         while( !found ) {
             pointsX[0] = (int) ( endPoint.getX( ) + Math.cos( angle ) * i * scale );
             pointsY[0] = (int) ( endPoint.getY( ) + Math.sin( angle ) * i * scale );
             if( !node.isInside( scale, new Point( pointsX[0], pointsY[0] ) ) )
                 found = true;
             else
                 i++;
         }
         // Obtain the second point of the arrow
         pointsX[1] = (int) ( pointsX[0] + Math.cos( angle + 0.4 ) * ARROW_SIZE * scale );
         pointsY[1] = (int) ( pointsY[0] + Math.sin( angle + 0.4 ) * ARROW_SIZE * scale );
 
         // Obtain the third point of the arrow
         pointsX[2] = (int) ( pointsX[0] + Math.cos( angle - 0.4 ) * ARROW_SIZE * scale );
         pointsY[2] = (int) ( pointsY[0] + Math.sin( angle - 0.4 ) * ARROW_SIZE * scale );
 
         g.fillPolygon( pointsX, pointsY, 3 );
     }
 
     public float getScale( ) {
 
         return scale;
     }
 
     public void setScale( float value ) {
 
         scale = value;
     }
 
     public void setSelectedNode( ConversationNodeView selectedNode ) {
 
         if( this.selectedNode != null )
             this.selectedNode.setSelected( false );
         this.selectedNode = null;
         for( GraphicNode node : graphicNodes ) {
             if( node.getNode( ) == selectedNode )
                 this.selectedNode = node;
         }
         if( this.selectedNode != null )
             this.selectedNode.setSelected( true );
     }
 
     public Point getSelectedNodePosition( ) {
 
         if( selectedNode != null ) {
             return selectedNode.getPosition( scale );
         }
         return null;
     }
 
     public void setSelectedChildNode( ConversationNodeView selectedNode ) {
 
         if( selectedChildNode != null )
             selectedChildNode.setSelectedChild( false );
         selectedChildNode = null;
         for( GraphicNode node : graphicNodes ) {
             if( node.getNode( ) == selectedNode )
                 selectedChildNode = node;
         }
         if( selectedChildNode != null )
             selectedChildNode.setSelectedChild( true );
     }
 
     /**
      * Set the links between the graphical nodes
      */
     private void updateLinksBetweenNodes( ) {
 
         // Get all the conversational nodes
         List<ConversationNodeView> nodes = graphConversationDataControl.getAllNodesViews( );
 
         // Link nodes
         for( int i = 0; i < graphicNodes.size( ); i++ ) {
             // Get the graphic node and the conversational node
             GraphicNode graphicNode = graphicNodes.get( i );
             ConversationNodeView node = nodes.get( i );
 
             // Remove old children in the current graphic node
             graphicNode.removeAllChildren( );
 
             // Link the graphic node to the other graphic nodes
             for( int j = 0; j < node.getChildCount( ); j++ ) {
                 // IMPORTANT: The graphic nodes and the conversational nodes are in the same position in each vector
                 int childPosition = nodes.indexOf( node.getChildView( j ) );
                 graphicNode.addChild( graphicNodes.get( childPosition ) );
             }
         }
     }
 
     /**
      * Updates the conversation size
      */
     private void updateConversationSize( ) {
 
         // Set intial values for the sides
         int right = Integer.MIN_VALUE;
         int left = Integer.MAX_VALUE;
         int top = Integer.MAX_VALUE;
         int bottom = Integer.MIN_VALUE;
 
         // Find the maximum and minimum values
         for( int i = 0; i < graphicNodes.size( ); i++ ) {
             Point currentNode = graphicNodes.get( i ).getPosition( scale );
             right = Math.max( right, currentNode.x );
             left = Math.min( left, currentNode.x );
             top = Math.min( top, currentNode.y );
             bottom = Math.max( bottom, currentNode.y );
         }
 
         // Add the margins
         right += CONVERSATION_MARGIN;
         left -= CONVERSATION_MARGIN;
         top -= CONVERSATION_MARGIN;
         bottom += CONVERSATION_MARGIN;
 
         int width = ( ( right - left ) );
         int height = ( ( bottom - top ) );
 
         // Set the conversation size and the upper left corner
         conversationSize = new Dimension( width, height );
         upperLeftCorner = new Point( ( left ), ( top ) );
     }
 
 }
