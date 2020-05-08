 /*
 Copyright 2013 (c) Illinois Tech Robotics <robotics.iit@gmail.com>
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 package common;
 
 public class Queue {
 	private class Node{
 		public Event data;
 		public Node next;
 		
 		public Node(Event d){
 			this.data = d;
 		}
 	}
 	
 	private int size;
 	private int max_size;
 	private Node first;
 	private Node last;
 	
 	public Queue(int ms){
 		this.max_size = ms;
 		this.size = 0;
 		this.first = null;
 		this.last = null;
 	}
 	
 	public synchronized int getSize(){
 		return this.size;
 	}
 	
 	public void put(Event d){
 		while(!this.add(d)){
 			try{
 				Thread.sleep(5);
 			}
 			catch(Exception e){
 				return;
 			}
 		}
 	}
 	
 	public synchronized boolean add(Event da){
 		if(size >= max_size){
 			return false;
 		}
 		Event newda = da.copy();
 		Node temp = new Node(newda);
 		size++;
 		if(last == null){
 			first = temp;
 			last = temp;
 		}
 		else{
 			last.next = temp;
 			last = temp;
 		}
 		Node cursor = first;
 		while(cursor!=null){
 			cursor = cursor.next;
 		}
 		return true;
 	}
 	
 	
 	public void putOverride(Event data){
 		while(!this.addOverride(data)){
 			try{
 				Thread.sleep(5);
 			}
 			catch(Exception e){
 				return;
 			}
 		}
 	}
 	
 	//overrides a command with the same command and index with the new value
 	public synchronized boolean addOverride(Event da){
 		Event newda = da.copy();
 		Node temp = new Node(newda);
 		Node cursor = first;
 		while(cursor != null){
 			if(cursor.data.getCommand() == temp.data.getCommand() &&
 					cursor.data.getIndex() == temp.data.getIndex()){
 				cursor.data.setValue(temp.data.getValue());
 				return true;
 			}
 			cursor = cursor.next;
 		}
 		if(size >= max_size){
 			return false;
 		}
 		size++;
 		if(last == null){
 			first = temp;
 			last = temp;
 		}
 		else{
 			last.next = temp;
 			last = temp;
 		}
 		return true;
 	}
 	
 	public Event take(){
 		Event temp = null;
 		while(true){
 			temp = this.poll();
 			if(temp == null){
 				try{
 					Thread.sleep(5);
 				}
 				catch(Exception e){
 					return null;
 				}
 			}
 			else{
 				return temp;
 			}
 		}	
 	}
 	
 	public synchronized Event poll(){
 		if(size == 0){
 			return null;
 		}
 		else{
 			Event tmp = null;
 			tmp = first.data;
 			first = first.next;
 			size --;
 			if(first == null){
 				last = null;
 			}
 			return tmp;
 		}
 	}
 	
 	public synchronized void flush(){
 		size = 0;
 		first = null;
 		last = null;
 	}
 }
