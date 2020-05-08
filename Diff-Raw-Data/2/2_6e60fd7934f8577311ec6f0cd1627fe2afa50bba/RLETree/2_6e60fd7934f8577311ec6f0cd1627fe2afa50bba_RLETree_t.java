 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 public class RLETree
 {
 	private class Node
 	{
 		public int pos;
 		public int data;
 
 		public Node next, prev;
 		public Node parent, left, right;
 		public int balance;
 
 		public Node( int pos, int data )
 		{
 			this.pos = pos;
 			this.data = data;
 		}
 
 		public String toString()
 		{
 			return "(" + pos + " " + data + ")";
 		}
 	}
 
 	Node head;
 	Node root;
 
 	public RLETree()
 	{
 		head = new Node( 0, 0 );
 		root = head;
 	}
 
 	/* Currently seeks through the linked list of runs */
 	Node seek_node( int pos )
 	{
 		// Stores the current node being looked at
 		Node node = head;
 
 		// Until completion
 		while ( true )
 		{
 			// If already at pos, return this one
 			if ( node.pos == pos )
 				return node;
 
 			// If this is the end of the list, return this one
 			if ( node.next == null )
 				return node;
 
 			// node.next is not null from here
 
 			// If the next node is greater than pos, return this one
 			if ( node.next.pos > pos )
 				return node;
 
 			// If the next node is closer to pos, move on there
 			if ( node.next.pos <= pos )
 				node = node.next;
 		}
 	}
 
 	// Will always succeed. pos is never less than 0 and runs stretch infinitely if unhindered
 	void set( int pos, int data )
 	{
 		if ( pos < 0 )
 		{
 			System.out.println( "Error! No runs starting at less than 0 allowed!" );
 			return;
 		}
 
 		Node node = new Node( pos, data );
 
 		// Get the node containing this position
 		Node start = seek_node( node.pos );
 		Node prev = start.prev;
 		Node next = start.next;
 
 		// Nothing to do, the position in this run already has that data
 		if ( start.data == node.data )
 			return;
 
 		// node.data != start.data from here
 
 		// If at the start of the run
 		if ( node.pos == start.pos )
 		{
 			// If the list has just one run
 			if ( prev == null && next == null )
 			{
 				start.pos += 1;
 				start.prev = node;
 				node.next = start;
 				head = node;
 				// Insert node to tree
 				return;
 			}
 
 			// If at the end of the list
 			if ( next == null )
 			{
 				start.pos += 1;
 				prev.next = node;
 				node.prev = prev;
 				node.next = start;
 				start.prev = node;
 				// Insert node to tree
 				// Try collapse prev to node
 				collapse( prev, node );
 				return;
 			}
 
 			// If this run is the first one
 			if ( start == head )
 			{
 				// If a single node at start, just change the data
 				if ( next.pos == start.pos + 1 )
 				{
 					start.data = node.data;
 					// Try collapse to next
 					collapse( node, next );
 					return;
 				}
 				else // Run is more than 1 in length
 				{
 					start.pos += 1;
 					node.next = start;
 					start.prev = node;
 					head = node;
 					// Insert node to tree
 					return;
 				}
 			}
 
 			// The run is somewhere inside the list
 
 			// If the run is 1 in length
 			if ( next.pos == start.pos + 1 )
 			{
 				start.data = data;
 				// Try to collapse to next
 				collapse( start, next );
 				// Try to collapse prev to node
 				collapse( prev, start );
 				return;
 			}
 
 			// The run is longer than 1 in length
 			start.pos += 1;
 			start.prev = node;
 			node.next = start;
 			node.prev = prev;
 			prev.next = node;
 			// Insert node to tree
 			// Try to collapse prev to node
 			collapse( prev, node );
 			return;
 		}
 		else // Somewhere out in a run, by definition in a run longer than 1 length
 		{
 			// If there is just one run in the list
 			if ( prev == null && next == null )
 			{
 				// Split the run. The old start, the new node and a new tail with same data as start
 				Node last = new Node( node.pos + 1, start.data );
 				start.next = node;
 				node.prev = start;
 				node.next = last;
 				last.prev = node;
 				// Insert node to tree
 				// Insert last to tree
 				return;
 			}
 
 			// If this is the first run
 			if ( start == head )
 			{
 				// If at the end of the run
 				if ( next.pos == node.pos + 1 )
 				{
 					node.next = next;
 					node.prev = start;
 					next.prev = node;
 					start.next = node;
 					// Insert node to tree
 					// Try to collapse node to next
 					collapse( node, next );
 					return;
 				}
 				else // Inside the first run of at least length 3
 				{
 					Node new_next = new Node( node.pos + 1, start.data );
 					start.next = node;
 					node.prev = start;
 					node.next = new_next;
 					new_next.prev = node;
 					new_next.next = next;
 					next.prev = new_next;
 					// Inset node to tree
 					// Insert new_next to tree
 					return;
 				}
 			}
 
 			// If this is the last run
 			if ( next == null )
 			{
 				Node new_last = new Node( node.pos + 1, start.data );
 				node.prev = start;
 				start.next = node;
 				node.next = new_last;
 				new_last.prev = node;
 				// Insert node to tree
 				// Insert new_last to tree
 				return;
 			}
 
 			// The run is somewhere inside the list
 
 			// If at the end of the run
 			if ( next.pos == node.pos + 1 )
 			{
 				node.next = next;
 				next.prev = node;
 				node.prev = start;
 				start.next = node;
 				// Insert node to tree
 				// Try to collapse node to next
 				collapse( node, next );
 				return;
 			}
 
 			// node.pos is somewhere inside a run of at least length 3
 			Node new_next = new Node( node.pos + 1, start.data );
 			node.next = new_next;
 			new_next.prev = node;
 			start.next = node;
 			node.prev = start;
 			new_next.next = next;
 			next.prev = new_next;
 			// Insert node to tree
 			// Insert new_next to tree
 			return;
 		}
 	}
 
 	private void collapse( Node a, Node b )
 	{
 		// Check that the links work( shouldn't ever fail if all else is done right )
 		if ( a.next == b && b.prev == a )
 		{
 			// a and b have the same data, merge to one run by removing b
 			if ( a.data == b.data )
 			{
 				a.next = b.next;
 
 				// Don't try to set null's prev
 				if ( b.next != null )
 					b.next.prev = a;
 
 				// Remove b from tree
 			}
 		}
 	}
 
 	public String toString()
 	{
 		Node node = head;
 		int count = 0;
 		String out = new String();
 
 		while ( node != null )
 		{
 			
 			out += node.toString();
 			node = node.next;
 			count++;
 		}
 
 		out += " " + count + " runs.";
 
 		return out;
 	}
 }
