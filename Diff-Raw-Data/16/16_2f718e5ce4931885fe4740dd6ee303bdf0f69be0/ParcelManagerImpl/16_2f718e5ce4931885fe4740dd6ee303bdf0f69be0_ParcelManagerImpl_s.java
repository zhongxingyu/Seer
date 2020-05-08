 /*******************************************************************************
  * Copyright 2011 Pascal Metrics
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.opensafety.hishare.managers.implementation.http;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.opensafety.hishare.dao.interfaces.ParcelDao;
 import org.opensafety.hishare.managers.interfaces.http.ParcelManager;
 import org.opensafety.hishare.managers.interfaces.http.PayloadManager;
 import org.opensafety.hishare.model.Parcel;
 import org.opensafety.hishare.util.interfaces.Encryption;
 import org.opensafety.hishare.util.interfaces.Encryption.CryptographyException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class ParcelManagerImpl implements ParcelManager
 {
 	@Autowired
 	private ParcelDao parcelDao;
 	@Autowired
 	private PayloadManager payloadManager;
 	@Autowired
 	private Encryption encryption;
 	
 	private int maximumParcelExpiration;
 	
 	Log log = LogFactory.getLog(this.getClass());
 	
 	public ParcelManagerImpl()
 	{
 		this(1);
 	}
 	
 	public ParcelManagerImpl(int parcelExpiration)
 	{
 		this.maximumParcelExpiration = parcelExpiration;
 	}
 	
 	public int getMaximumParcelExpiration()
 	{
 		return maximumParcelExpiration;
 	}
 	
 	public void setMaximumParcelExpiration(int maximumParcelExpiration)
 	{
 		this.maximumParcelExpiration = maximumParcelExpiration;
 	}
 	
 	private boolean notExpired(Parcel parcel)
     {
 		Date expiration = parcel.getExpirationDate();
 		Date now = Calendar.getInstance().getTime();
 		
 		return now.before(expiration);
     }
 	
 	public boolean deleteParcel(Parcel parcel)
 	{
 		boolean deletePayload = payloadManager.deletePayload(parcel.getPayloadLocation());
 		boolean deleteParcel = parcelDao.deleteParcel(parcel);
 		return deletePayload && deleteParcel;
 	}
 	
 	public byte[] downloadPayload(Parcel parcel)
 	{
 		//seriously, you can't download after expiration
 		if(notExpired(parcel))
 		{
 			return payloadManager.downloadPayload(parcel);
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public Parcel getParcel(String parcelId, String parcelPassword)
 	{
 		if(parcelDao.verifyParcelAvailable(parcelId))
 		{
 			Parcel parcel = parcelDao.getById(parcelId);
 			try
 			{
 				byte[] hashedPassword = encryption.hashPassword(parcelPassword, parcel.getSalt());
 				if(Arrays.equals(hashedPassword, parcel.getHashedPassword()))
 				{
 					parcel.setPassword(parcelPassword);
 					return parcel;
 				}
 				else
 				{
 					return null;
 				}
 			}
 			catch(CryptographyException e)
 			{
 				log.error("Password Matching Threw Exception!", e);
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	public void persistParcel(Parcel parcel, byte[] payload)
 	{
 		parcelDao.addParcel(parcel);
 		payloadManager.persistPayload(parcel, payload);
 	}
 	
 	public void updateParcel(Parcel parcel)
 	{
 		parcelDao.updateParcel(parcel);
 	}
 	
 	/*
 	 * Verify that this parcel id exists and that the password is correct
 	 */
 	public boolean verifyParcelAvailable(String parcelId, String parcelPassword)
 	{
 		if(parcelDao.verifyParcelAvailable(parcelId))
 		{
 			Parcel parcel = parcelDao.getById(parcelId);
 			if(notExpired(parcel))
 			{
 				try
 				{
 					byte[] hashedPassword = encryption.hashPassword(parcelPassword, parcel.getSalt());
 					return Arrays.equals(hashedPassword, parcel.getHashedPassword());
 				}
 				catch(CryptographyException e)
 				{
 					log.error("Password Matching Threw Exception!",e);
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean verifyDaysToLive(int daysToLive)
     {
 		return daysToLive <= maximumParcelExpiration;
     }
 }
