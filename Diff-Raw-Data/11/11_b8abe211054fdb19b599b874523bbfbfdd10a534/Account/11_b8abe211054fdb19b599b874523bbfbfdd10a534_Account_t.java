 /***************************************************************************
     begin........: January 2012
     copyright....: Sebastian Fedrau
     email........: lord-kefir@arcor.de
  ***************************************************************************/
 
 /***************************************************************************
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
  ***************************************************************************/
 package accounting.application;
 
 import java.util.*;
 import accounting.application.annotation.*;
 import accounting.data.ProviderException;
 
 @SuppressWarnings("unused")
 public class Account extends AEntity<Long> implements Comparable<Account>
 {
 	@Attribute(name="Name", readable=true, writeable=true)
 	@StringValidator(allowNull=false, minLength=1, maxLength=32)
 	private String name;
 	@Attribute(name="Remarks", readable=true, writeable=true)
 	@StringValidator(allowNull=true, minLength=0, maxLength=512)
 	private String remarks;
 	@Attribute(name="Currency", readable=true, writeable=true)
 	@ObjectValidator(allowNull=false)
 	private Currency currency;
 
 	@Override
 	protected void update() throws ProviderException
 	{
 		provider.updateAccount(this);
 	}
 
 	@Override
 	protected void remove() throws ProviderException, ReferenceException
 	{
 		provider.deleteAccount(this.getId());
 	}
 
 	public Transaction createTransaction(Category category, Date date, Double amount, String no, String remarks) throws ProviderException
 	{
 		return provider.createTransaction(this, category, date, amount, no, remarks);
 	}
 
 	public Transaction getTransaction(long id) throws ProviderException
 	{
 		return provider.getTransaction(id, this);
 	}
 
 	public List<Transaction> getTransactions(Date begin, Date end) throws ProviderException
 	{
 		return provider.getTransactions(this, begin.getTime(), end.getTime());
 	}
 
 	public List<Long> getTimestamps() throws ProviderException
 	{
 		return provider.getTimestamps(this.getId());
 	}
 
 	public double getOpeningBalance(int year, int month) throws ProviderException
 	{
 		Calendar calendar = Calendar.getInstance();
 
 		calendar.set(year, month, 1, 0, 0, 0);
 
 		return provider.getBalance(getId(), calendar.getTime());
 	}
 
 	public double getClosingBalance(int year, int month) throws ProviderException
 	{
 		Calendar calendar = Calendar.getInstance();
 
		calendar.set(year, month, 1, 23, 59, 59);
 		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
 
 		return provider.getBalance(getId(), calendar.getTime());
 	}
 
 	public String getName()
 	{
 		return getString("Name");
 	}
 
 	public void setName(String name) throws AttributeException
 	{
 		setAttribute("Name", name);
 	}
 
 	public String getRemarks()
 	{
 		return getString("Remarks");
 	}
 
 	public void setRemarks(String remarks) throws AttributeException
 	{
 		setAttribute("Remarks", remarks);
 	}
 
 	public Currency getCurrency()
 	{
 		return (Currency)getObject("Currency");
 	}
 
 	public void setCurrency(Currency currency) throws AttributeException
 	{
 		setAttribute("Currency", currency);
 	}
 
 	@Override
 	public String toString()
 	{
 		return getName();
 	}
 
 	@Override
 	public boolean equals(Object object)
 	{
 		if(object instanceof Account)
 		{
 			return ((Account)object).getId().equals(getId());
 		}
 
 		return false;
 	}
 
 	@Override
 	public int compareTo(Account account)
 	{
 		return toString().compareToIgnoreCase(account.toString());
 	}
 }
