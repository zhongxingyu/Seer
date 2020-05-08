 package com.shealevy.android.model;
 
 public class AndroidModel<T> {
	public Object get(TableDelegateField<T> field) {
		if(field.type().equals(byte.class)) {
			return (byte) 0;
		} else {
			return null;
		}
 	}
 }
