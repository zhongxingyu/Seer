 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import java.util.*;
 
 /**
  * Bridges a system that expects to read Ackable objects to a system that doesn't a system
  * that provides them. This class wraps a WriteHead that deals with Ackable objects and
  * automatically wraps all bare payloads given to it with ackable objects.
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  * @param <$PAYLOAD>
  */
 public class AckableWriteHeadBridge<$PAYLOAD> implements WriteHead<$PAYLOAD> {
 	public AckableWriteHeadBridge(WriteHead<Ackable<$PAYLOAD>> $ackableHead) {
 		$wrap = $ackableHead;
 	}
 	
 	WriteHead<Ackable<$PAYLOAD>>	$wrap;
 	
 	public void write($PAYLOAD $chunk) {
 		$wrap.write(new Ackable<$PAYLOAD>($chunk));
 	}
 	
 	public void writeAll(Collection<? extends $PAYLOAD> $chunks) {
		Collection<Ackable<$PAYLOAD>> $bunches = new ArrayList<Ackable<$PAYLOAD>>();
 		for ($PAYLOAD $chunk : $chunks)
 			$bunches.add(new Ackable<$PAYLOAD>($chunk));
 		$wrap.writeAll($bunches);
 	}
 	
 	public boolean hasRoom() {
 		return $wrap.hasRoom();
 	}
 	
 	public boolean isClosed() {
 		return $wrap.isClosed();
 	}
 	
 	public void close() {
 		$wrap.close();
 	}	
 }
