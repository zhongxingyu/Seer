 /*
   * JBoss, Home of Professional Open Source
   * Copyright 2007, JBoss Inc., and individual contributors as indicated
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
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
 package org.jboss.security.xacml.bridge;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.security.xacml.sunxacml.AbstractPolicy;
 import org.jboss.security.xacml.sunxacml.EvaluationCtx;
 import org.jboss.security.xacml.sunxacml.MatchResult;
 import org.jboss.security.xacml.sunxacml.PolicyMetaData;
 import org.jboss.security.xacml.sunxacml.PolicySet;
 import org.jboss.security.xacml.sunxacml.VersionConstraints;
 import org.jboss.security.xacml.sunxacml.ctx.Status;
 import org.jboss.security.xacml.sunxacml.finder.PolicyFinder;
 import org.jboss.security.xacml.sunxacml.finder.PolicyFinderModule;
 import org.jboss.security.xacml.sunxacml.finder.PolicyFinderResult;
 
 /**
 *  PolicyFinderModule for PolicySet
 *  @author Anil.Saldhana@redhat.com
*  @since  Jul 6, 2007 
 *  @version $Revision$
 */
 @SuppressWarnings({"unused"})
 public class PolicySetFinderModule extends PolicyFinderModule
 {
    private PolicySet policySet;
 
   private List<AbstractPolicy> policies = new ArrayList<AbstractPolicy>();
 
    protected PolicyFinder policyFinder = null;
 
    public PolicySetFinderModule()
    {
    }
 
    public PolicySetFinderModule(PolicySet policySet)
    {
       this.policySet = policySet;
    }
 
    public PolicySetFinderModule(PolicySet policySet, List<AbstractPolicy> policies)
    {
       this.policySet = policySet;
      this.policies.addAll(policies);
    }
 
    @Override
    public void init(PolicyFinder finder)
    {
       this.policyFinder = finder;
    }
 
    /**
      * Finds the applicable policy (if there is one) for the given context.
      *
      * @param context the evaluation context
      *
      * @return an applicable policy, if one exists, or an error
      */
    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context)
    {
       AbstractPolicy selectedPolicy = null;
       MatchResult match = policySet.match(context);
       int result = match.getResult();
 
       // if target matching was indeterminate, then return the error
       if (result == MatchResult.INDETERMINATE)
          return new PolicyFinderResult(match.getStatus());
 
       // see if the target matched
       if (result == MatchResult.MATCH)
       {
          // see if we previously found another match
          if (selectedPolicy != null)
          {
             // we found a match before, so this is an error
             ArrayList<String> code = new ArrayList<String>();
             code.add(Status.STATUS_PROCESSING_ERROR);
             Status status = new Status(code, "too many applicable " + "top-level policies");
             return new PolicyFinderResult(status);
          }
 
          // this is the first match we've found, so remember it
          selectedPolicy = policySet;
       }
 
       // return the single applicable policy (if there was one)
       return new PolicyFinderResult(selectedPolicy);
    }
 
    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
          PolicyMetaData parentMetaData)
    {
      for (AbstractPolicy p : policies)
      {
         if (p.getId().compareTo(idReference) == 0)
            return new PolicyFinderResult(p);
      }
       return new PolicyFinderResult();
    }
 
    @Override
    public boolean isRequestSupported()
    {
       return true;
    }
 
    /**
      * Always returns true, since reference-based retrieval is supported.
      *
      * @return true
      */
    public boolean isIdReferenceSupported()
    {
       return true;
    }
 
    public void set(PolicySet ps, List<AbstractPolicy> policies)
    {
       this.policySet = ps;
      this.policies = policies;
    }
 }
