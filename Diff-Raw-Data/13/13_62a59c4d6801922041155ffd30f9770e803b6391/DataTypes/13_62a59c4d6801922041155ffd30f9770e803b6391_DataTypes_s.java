 package com.grover.mingebag.ic;
 
 public enum DataTypes {
	STRING ((byte) 1),
	NUMBER ((byte) 2),
	PLAYER ((byte) 3),
	ITEM ((byte) 4);
	
	byte id;
	DataTypes(byte id) {
		this.id = id;
	}
 }
