 package org.tophat.android.model;
 
 import java.util.Map;
 
 import org.tophat.android.exceptions.HttpException;
 import org.tophat.android.mapping.Mapping;
 import org.tophat.android.networking.ApiCommunicator;
 
 public abstract class Mapper {
 
 	private ApiCommunicator apic;
 
 	public Mapper(ApiCommunicator apic)
 	{
 		this.setApiCommunicator(apic);
 	}
 	
 	protected Mapping creator(Map<String, Object> data)
 	{
 		return new Mapping(data);
 	}
 
 	public Mapping get(Mapping m, Integer id) throws HttpException
 	{
		return this.creator(this.getApiCommunicator().get(m.getAccessUrl()+"/"+m.getId()));
 	}
 	
 	public Mapping create(Mapping m) throws HttpException
 	{
 		if ( m.getId() == null )
 		{
 			return this.creator(this.getApiCommunicator().post(m.getAccessUrl(), m.getMap()));
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	public Mapping update(Mapping m) throws HttpException
 	{
 		if ( m.getId() != null )
 		{
 			return this.creator(this.getApiCommunicator().put(m.getAccessUrl()+"/"+m.getId(), m.getMap()));
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param m
 	 * @return
 	 * @throws HttpException
 	 */
 	public boolean delete(Mapping m) throws HttpException
 	{
 		if (m.getAccessUrl() != null )
 		{
 			this.getApiCommunicator().delete(m.getAccessUrl()+"/"+m.getId());
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * @return the apic
 	 */
 	public ApiCommunicator getApiCommunicator() {
 		return apic;
 	}
 
 	/**
 	 * @param apic the apic to set
 	 */
 	public void setApiCommunicator(ApiCommunicator apic) {
 		this.apic = apic;
 	}
 }
