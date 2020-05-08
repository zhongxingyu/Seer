 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 // Copyright (C) 2000  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.engine.process;
 
 /** Package scope */
 class MethodStatistics
 {
     private long m_transactions = 0;
     private long m_totalTime = 0;
     private long m_errors = 0;
 
     public MethodStatistics()
     {
     }
 
     public synchronized void addTransaction(long time)
     {
 	m_transactions++;
 	m_totalTime += time;
     }
 	
     public synchronized void addError()
     {
 	m_errors++;
     }
 
     /**
      * Assumes we don't need to synchronise access to snapshot.
      */
     public synchronized MethodStatistics getDelta(MethodStatistics snapshot,
 						  boolean updateSnapshot)
     {
 	final MethodStatistics result = new MethodStatistics(
 	    snapshot.m_transactions - m_transactions,
 	    snapshot.m_totalTime - m_totalTime,
 	    snapshot.m_errors - m_errors);
 
 	if (updateSnapshot) {
 	    snapshot.m_transactions = m_transactions;
 	    snapshot.m_totalTime = m_totalTime;
 	    snapshot.m_errors = m_errors;
 	}
 
 	return result;
     }
 
     /**
      * Assumes we don't need to synchronise access to operand.
      */
    public synchronized MethodStatistics add(MethodStatistics operand)
     {
	return new MethodStatistics(m_transactions + operand.m_transactions,
				    m_totalTime + operand.m_totalTime,
				    m_errors + operand.m_errors);
     }
     
     private MethodStatistics(long transactions, long totalTime,
 			     long errors)
     {
 	m_transactions = transactions;
 	m_totalTime = totalTime;
 	m_errors = errors;
     }
 
     /** Accessor. N.B. Use clone() to get a consistent snapshot of a
      * changing MethodStatistics */
     public long getTransactions() 
     {
 	return m_transactions;
     }
 
     /** Accessor. N.B. Use clone() to get a consistent snapshot of a
      * changing MethodStatistics */
     public long getTotalTime()
     {
 	return m_totalTime;
     }
 
     /** Accessor. N.B. Use clone() to get a consistent snapshot of a
      * changing MethodStatistics */
     public long getErrors()
     {
 	return m_errors;
     }
 
     public synchronized double getAverageTransactionTime()
     {
 	if (m_transactions == 0) {
 	    return 0d; // Not really sensible, but ICGE at the moment.
 	}
 	else {
 	    return m_totalTime/(double)m_transactions;
 	}
     }
 }
