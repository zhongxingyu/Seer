 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: DefaultXdmComponent.java,v 1.6 2003-08-31 14:04:38 shahid.shah Exp $
  */
 
 package com.netspective.commons.xdm;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.io.File;
 import java.io.IOException;
 
 import com.netspective.commons.io.InputSourceTracker;
 import com.netspective.commons.xdm.XdmComponent;
 import com.netspective.commons.metric.Metrics;
 import com.netspective.commons.metric.MetricsGroup;
 import com.netspective.commons.metric.MetricsProducer;
 import com.netspective.commons.metric.Metric;
 
 public abstract class DefaultXdmComponent implements XdmComponent, MetricsProducer
 {
     private InputSourceTracker inputSource;
     private List lifecycleListeners = new ArrayList();
     private List errors = new ArrayList();
     private List warnings = new ArrayList();
     private Metrics metrics;
     private long loadDuration; // time it took to load/parse
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void removedFromCache(Map cache, Object key, int flags)
     {
         List listeners = getLifecycleListeners();
         if(listeners.size() > 0)
         {
             XdmComponentEvent event = new XdmComponentEvent(this, flags);
             for(int i = 0; i < listeners.size(); i++)
                 ((XdmComponentLifecyleListener) listeners.get(i)).xdmComponentRemovedFromCache(event);
         }
     }
 
     public void addedToCache(Map cache, Object key, int flags)
     {
         List listeners = getLifecycleListeners();
         if(listeners.size() > 0)
         {
             XdmComponentEvent event = new XdmComponentEvent(this, flags);
             for(int i = 0; i < listeners.size(); i++)
                 ((XdmComponentLifecyleListener) listeners.get(i)).xdmComponentAddedToCache(event);
         }
     }
 
     public void loadedFromXml(int flags)
     {
         List listeners = getLifecycleListeners();
         if(listeners.size() > 0)
         {
             XdmComponentEvent event = new XdmComponentEvent(this, flags);
             for(int i = 0; i < listeners.size(); i++)
                 ((XdmComponentLifecyleListener) listeners.get(i)).xdmComponentLoadedFromXml(event);
         }
     }
 
     public List getLifecycleListeners()
     {
         return lifecycleListeners;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     /**
      * This method will be called by the DataModelSchema parse as soon as the root element is started and will provide
      * the input source that is producing this data.
      * @param source The input source (could be a file or other source)
      */
     public void setInputSourceTracker(InputSourceTracker source)
     {
         inputSource = source;
     }
 
     public InputSourceTracker getInputSource()
     {
         return inputSource;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public List getErrors()
     {
         return errors;
     }
 
     public List getWarnings()
     {
         return warnings;
     }
 
     public void printErrorsAndWarnings()
     {
         if(errors.size() != 0)
         {
             System.out.println("*** ERRORS: " + errors.size());
             for(int i = 0; i < errors.size(); i++)
             {
                 Object error = errors.get(i);
                 if(error instanceof Throwable)
                     System.out.println(((Throwable) error).getMessage());
                 else
                     System.out.println(error.toString());
             }
         }
     }
 
    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public boolean hasWarnings()
    {
        return warnings.size() > 0;
    }

    public boolean hasErrorsOrWarnings()
    {
        return errors.size() > 0 || warnings.size() > 0;
    }

     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void produceMetrics(Metric parent)
     {
         MetricsGroup input = parent.addGroupMetric("Input Source");
         input.addValueMetric("Type", getInputSource().getClass().getName());
         input.addValueMetric("Identifier", getInputSource().getIdentifier());
         input.addValueMetric("Load time", Double.toString(getLoadDuration() / 1000.0) + " seconds");
         input.addValueMetric("Dependencies", Long.toString(getInputSource().getDependenciesCount()));
         input.addValueMetric("Errors", Long.toString(errors.size()));
         input.addValueMetric("Warnings", Long.toString(warnings.size()));
     }
 
     public Metrics getMetrics()
     {
         if(metrics == null)
         {
             metrics = new Metrics(this, this.getClass().getName() + " Metrics");
             produceMetrics(metrics);
         }
 
         return metrics;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void generateIdentifiersConstants(File rootPath, String rootPkgAndClassName) throws IOException
     {
         throw new RuntimeException("Not implemented.");
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public long getLoadDuration()
     {
         return loadDuration;
     }
 
     public void setLoadDuration(long startTime, long endTime)
     {
         this.loadDuration = endTime - startTime;
     }
 }
