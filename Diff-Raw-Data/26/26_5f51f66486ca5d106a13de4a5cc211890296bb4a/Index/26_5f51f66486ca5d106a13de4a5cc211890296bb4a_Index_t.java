 package com.example.plaid.pages;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 public class Index
 {
 
     @Inject
     Messages messages;
 
    @Property
    private String currentPrime;

     public String getText()
     {
         return messages.get("key-used-in-page-klass");
     }
 
    public Collection<String> getPrimeNumbers()
    {
        List<String> primes = new ArrayList<String>();
        primes.add("2");
        primes.add("3");
        primes.add("5");
        primes.add("7");
        primes.add("11");
        return primes;
    }

    public String getPlaidPropertyOne()
    {
        return "I may not have gone where I intended to go, but I think I have ended up where I needed to be.";
    }

 }
