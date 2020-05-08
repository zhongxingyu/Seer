 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.util.*;
 import java.io.*;
 
 public class Reader<I> implements Generator {
 	
 	interface InputStreamOpener {
 		public InputStream open() throws IOException;
 	}
 	
 	long cur_time;
 	long prev_time;
 	long next_time;
 	long seek_interval;
 	
 	ItemFactory<I> _factory;
 	List<I> buffer;
 	Bus<I> _bus = new Bus<I>();
 	int _priority;
 	long _offset;
 	
 	boolean has_next_time;
 	boolean has_next_item;
 	
 	BufferedReader reader;
 	InputStreamOpener opener;
 	String next_line;
 	
 	Store _store;
 	
 	Reader(Store store, InputStreamOpener inputStreamOpener, long seekInterval, ItemFactory<I> factory, int priority, long offset) throws IOException{
 		opener = inputStreamOpener;
 		_offset = offset;
 		seek_interval = seekInterval;
 		_factory = factory;
 		_priority = priority;
 		_store = store;
 		init();
 	}
 	
 	@Override
 	public void seek(long time) throws IOException {
 		fastSeek(time+_offset, seek_interval);
 		while ( has_next_time && next_time < time+_offset ){
 			next();
 		}
 		cur_time = time+_offset;
 	}
 	
 	public void incr(long incr_time) throws IOException {
 		cur_time += incr_time;
 		while ( cur_time> next_time){
 			step();
 			_bus.queue(prev_time-_offset,buffer);
 		}
 	}
 	
 	public long time(){
 		return cur_time-_offset;
 	}
 	
 	public long nextTime(){
 		return next_time-_offset;
 	}
 	
 	public long previousTime(){
 		return prev_time-_offset;
 	}
 	
 	public void close() throws IOException {
 		reader.close();
 		_store.notifyClose(this);
 	}
 	
 
 	public boolean hasNext() {
 		return has_next_time;
 	}
 	
 	public List<I> next() throws IOException {
 		step();
 		cur_time = prev_time;
 		return buffer;
 	}
 	
 	private void step() throws IOException {
 		stepNextTime();
 		buffer = new LinkedList<I>(); 
 		while ( has_next_item ){
 			String next = getNextItem();
 			buffer.add( _factory.fromString(next) );
 		}
 	}
 	
 	public List<I> previous(){
 		return buffer;
 	}
 	
 	
 	public Bus<I> bus() {
 		return _bus;
 	}
 
 	public void setBus(Bus<I> bus) {
 		_bus = bus;
 	}
 	
 	private void peekLine() throws IOException {
 		next_line = reader.readLine();
 		if ( next_line == null ){ // end of file has been reached
 			has_next_time = false;
 			has_next_item = false;
 			next_time = Trace.INFINITY;
 			return;
 		}
 		
 		try {
 			long time = Long.parseLong(next_line);
 			next_time = time;
 			has_next_item = false;
 			has_next_time = true;
 		} catch ( NumberFormatException nfe ){
 			has_next_item = true;
 			has_next_time = false;
 		} 
 	}
 	
 	private void init() throws IOException{
 		_store.notifyOpen(this);
 		reader = new BufferedReader ( new InputStreamReader(opener.open()) );
 		prev_time = -Trace.INFINITY;
 		buffer = Collections.emptyList();
 		cur_time = prev_time;
 		peekLine();
 	}
 
 	private void reset() throws IOException {
 		close();
 		init();
 	}
 	
 	private void stepNextTime() throws IOException {
 		while ( ! has_next_time ){
 			peekLine();
 		}
 		prev_time = next_time;
 		peekLine();
 	}
 	
 	private String getNextItem() throws IOException {
 		if ( ! has_next_item )
 			return null;
 		
 		String item = next_line;
 		peekLine();
 		return item;
 	}
 	
 	private void fastSeek(long time, long interval) throws IOException {
 		long target_time = time - interval;
		if ( prev_time > target_time ){
 			reset();
 		}
 		while ( true ){
 			if ( has_next_time && next_time >= target_time ){ // we have found the right place to stop seeking
 				break;
 			} else if ( ! has_next_time && ! has_next_item ){ // we have reached end of file
 				break;
 			}
 			peekLine();
 		}
 	}
 
 	@Override
 	public Bus<?>[] busses() {
 		return new Bus<?>[]{_bus};
 	}
 
 	@Override
 	public int priority() {
 		return _priority;
 	}
 }
