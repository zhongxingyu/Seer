 /* 
  * Copyright (c) 2011 Accenture
  * Licensed under the MIT open source license
  * http://www.opensource.org/licenses/mit-license.php
  */
 package no.uka.findmyapp.android.rest.mapper;
 
 import java.io.Serializable;
 import java.util.List;
 
 import no.uka.findmyapp.android.rest.contracts.Location.LocationContract;
 import no.uka.findmyapp.android.rest.datamodels.models.Location;
 import android.content.ContentValues;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class LocationContentMapper.
  */
 public class LocationContentMapper implements IContentMapper {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.uka.findmyapp.android.rest.mapper.IContentMapper#isList()
 	 */
 	@Override
 	public boolean isList() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * no.uka.findmyapp.android.rest.mapper.IContentMapper#mapValues(java.io
 	 * .Serializable)
 	 */
 	@Override
 	public ContentValues mapValues(Serializable object) {
 		Location temp = (Location) object;
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(LocationContract.LOCATION_ID, temp.getLocationId());
		contentValues.put(LocationContract.LOCATION_STRING_ID, LocationContract.LOCATION_STRING_ID);
 		contentValues.put(LocationContract.LOCATION_NAME, temp.getLocationName());
 
 		return contentValues;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * no.uka.findmyapp.android.rest.mapper.IContentMapper#mapValuesList(java
 	 * .io.Serializable)
 	 */
 	@Override
 	public List<ContentValues> mapValuesList(Serializable object) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
