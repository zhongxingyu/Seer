 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.stormfront.script.wsl;
 
 import java.util.List;
 
 public class WSLRelationalCondition extends WSLAbstractBoolean {
 
 	public enum RelationalOperator {
 		GreaterThan			{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toDouble() > arg2.toDouble(); } },
 		GreaterThanEqualTo	{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toDouble() >= arg2.toDouble(); } },
 		LessThan			{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toDouble() < arg2.toDouble(); } },
 		LessThanEqualTo		{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toDouble() <= arg2.toDouble(); } },
 		Contains			{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toString().contains(arg2.toString()); } },
 		ContainsRe			{ boolean compare(IWSLValue arg1, IWSLValue arg2) { return arg1.toString().matches(arg2.toString()); } };
 	
 		abstract boolean compare(IWSLValue arg1, IWSLValue arg2);
 	}
 	
 	private List<IWSLValue> args;
 	private List<RelationalOperator> ops;
 	
	public WSLRelationalCondition(List<IWSLValue> args, List<RelationalOperator> ops) {
 		this.args = args;
 		this.ops = ops;
 	}
 	
 	@Override
 	public boolean toBoolean() {
 		IWSLValue value = args.get(0);
 		for(int i = 0; i < ops.size(); i++) {
 			IWSLValue nextValue = args.get(i + 1);
			if(!ops.get(i).compare(value, nextValue))
 				return false;
 			value = nextValue;
 		}
 		return true;
 	}
 }
