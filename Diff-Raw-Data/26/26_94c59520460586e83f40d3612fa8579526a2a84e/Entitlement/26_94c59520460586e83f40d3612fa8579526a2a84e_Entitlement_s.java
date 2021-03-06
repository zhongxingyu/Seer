 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.billing.entitlement.api;
 
 import com.ning.billing.catalog.api.BillingActionPolicy;
 import com.ning.billing.catalog.api.BillingPeriod;
 import com.ning.billing.catalog.api.Plan;
 import com.ning.billing.catalog.api.PlanPhase;
 import com.ning.billing.catalog.api.PriceList;
 import com.ning.billing.catalog.api.Product;
 import com.ning.billing.catalog.api.ProductCategory;
 import com.ning.billing.util.callcontext.CallContext;
 import com.ning.billing.util.callcontext.TenantContext;
 import com.ning.billing.util.entity.Entity;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import java.util.UUID;
 
 /**
  * An Entitlement is created using the <code>EntitlementApi</code>
  * <p/>
  * It contains all the catalog information and current state that answers the entitlement question.
  * <p>
  * The users of that API will control all the entitlement behavior when making changes such as the effectiveDate, catalog info,...
  * By default the system will use system wide policies to control the billing aspect, but specific APIs also allow to override those.
  * <p>
  * @see com.ning.billing.entitlement.api.EntitlementApi
  */
 public interface Entitlement extends Entity {
 
     /**
      * Used to control the effective date that should be used on Plan change or on cancellation.
      */
     public enum EntitlementActionPolicy {
         /* Immediate */
         IMMEDIATE,
         /* End of Term */
         END_OF_TERM
     }
 
     /**
      * Possible states of an <code>Entitlement</code>
      */
     public enum EntitlementState {
         /* The entitlement was created in that initial state */
         ACTIVE,
         /* The system blocked the entitlement */
         BLOCKED,
         /* The user cancelled the entitlement */
         CANCELLED
     }
 
     /**
      * Possible <code>Entitlement</code> source types.
      */
     public enum EntitlementSourceType {
         NATIVE,
         MIGRATED,
         TRANSFERRED
     }
 
     /**
      * @return the unique id of the base entitlement
      */
     public UUID getBaseEntitlementId();
 
 
     /**
      *
      * @return the unique Id of the SubscriptionBundle
      */
     public UUID getBundleId();
     /**
      *
      * @return the account id
      */
     public UUID getAccountId();
 
     /**
      *
      * @return the external key that was supplied when creating the base entitlement
      */
     public String getExternalKey();
 
     /**
      * @return the state of the entitlement
      */
     public EntitlementState getState();
 
     /**
      * @return the entitlement source type
      */
     public EntitlementSourceType getSourceType();
 
     /**
      * @return the start date of the entitlement
      */
     public LocalDate getEffectiveStartDate();
 
     /**
      * @return the end date of the entitlement, that is the date at which it got cancelled.
      */
     public LocalDate getEffectiveEndDate();
 
     /**
      * @return the current <code>Product</code>
      */
     public Product getProduct();
 
     /**
      * @return the current <code>Plan</code>
      */
     public Plan getPlan();
 
     /**
      * @return the current <code>PriceList></code>
      */
     public PriceList getPriceList();
 
     /**
      * @return the current <Phase></Phase>
      */
     public PlanPhase getCurrentPhase();
 
     /**
      * @return the current <code>ProductCategory</code>
      */
     public ProductCategory getProductCategory();
 
     /**
      * @return the last <code>Product</code> prior to cancellation
      */
     public Product getLastActiveProduct();
 
     /**
      * @return the last <code>Plan</code> prior to cancellation
      */
     public Plan getLastActivePlan();
 
     /**
      * @return the last active PriceList
      */
     public PriceList getLastActivePriceList();
 
     /**
      * @return the last active ProductCategory
      */
     public ProductCategory getLastActiveProductCategory();
 
     /**
      * Cancels the <code>Entitlement</code> at the specified date.
      * After this operation, the existing object becomes stale.
      *
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param effectiveDate the date at which the entitlement should be cancelled
      * @param context       the context
      *
      * @return the new <code>Entitlement</code> after the cancellation was performed
      * @throws EntitlementApiException if cancellation failed
      */
     public Entitlement cancelEntitlementWithDate(final LocalDate effectiveDate, final CallContext context)
             throws EntitlementApiException;
 
 
     /**
      * Cancel the <code>Entitlement</code> with a policy.
      * After this operation, the existing object becomes stale.
      *
      * @param policy  the policy that is used by the system to calculate the cancellation date
      * @param context the context
      * @return the new <code>Entitlement</code> after the cancellation was performed
      * @throws EntitlementApiException if cancellation failed
      */
     public Entitlement cancelEntitlementWithPolicy(final EntitlementActionPolicy policy, final CallContext context)
             throws EntitlementApiException;
 
 
     /**
      * Cancels the <code>Entitlement</code> at the specified date
      * After this operation, the existing object becomes stale.
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param effectiveDate the date at which the entitlement should be cancelled
      * @param billingPolicy the billingPolicy
      * @param context       the context
      * @return the new <code>Entitlement</code> after the cancellation was performed
      * @throws EntitlementApiException if cancellation failed
      */
     public Entitlement cancelEntitlementWithDateOverrideBillingPolicy(final LocalDate effectiveDate, final BillingActionPolicy billingPolicy, final CallContext context)
             throws EntitlementApiException;
 
 
     /**
      * Cancels the <code>Entitlement</code> at the specified date and overrides the default billing policy.
      * After this operation, the existing object becomes stale.
      *
      * @param policy        the policy that is used by the system to calculate the cancellation date
      * @param billingPolicy the override billing policy
      * @param context       the context
      * @return the new <code>Entitlement</code> after the cancellation was performed
      *
      * @throws EntitlementApiException if cancellation failed
      */
     public Entitlement cancelEntitlementWithPolicyOverrideBillingPolicy(final EntitlementActionPolicy policy, final BillingActionPolicy billingPolicy, final CallContext context)
             throws EntitlementApiException;
 
 
    /**
     * Uncancel a previously cancelled <code>Entitlement</code>. This will only work if the effective date of the cancellation is in the future, that is, the cancellation
     * did not occur yet.
     *
     * @param context       the context
     *
     * @throws EntitlementApiException if the <code>Entitlement</code> is not in an <tt>ACTIVE</tt> state or if it's cancellation date is not in the future.
     */
    public void uncancel(final CallContext context)
            throws EntitlementApiException;
 
     /**
      * Change <code>Entitlement</code> plan at the specified date.
      * After this operation, the existing object becomes stale.
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param productName   the new product name
      * @param billingPeriod the new billing period
      * @param priceList     the new priceList
      * @param effectiveDate the date at which the entitlement should be changed
      * @param context       the context
      * @return the new <code>Entitlement</code> after the change was performed
      * @throws EntitlementApiException if change failed
      */
     public Entitlement changePlan(final String productName, final BillingPeriod billingPeriod, final String priceList, final LocalDate effectiveDate, final CallContext context)
             throws EntitlementApiException;
 
     /**
      * Change <code>Entitlement</code> plan at the specified date and overrides the billing policy.
      * After this operation, the existing object becomes stale.
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param productName   the new product name
      * @param billingPeriod the new billing period
      * @param priceList     the new priceList
      * @param effectiveDate the date at which the entitlement should be changed
      * @param billingPolicy the override billing policy
      * @param context       the context
      * @return the new <code>Entitlement</code> after the change was performed
      * @throws EntitlementApiException if change failed
      */
     public Entitlement changePlanOverrideBillingPolicy(final String productName, final BillingPeriod billingPeriod, final String priceList, final LocalDate effectiveDate,
                                                    final BillingActionPolicy billingPolicy, final CallContext context)
             throws EntitlementApiException;
 
 
     /**
      * Pauses an <code>Entitlement</code> until it gets resumed.
      * After this operation, the existing object becomes stale.
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param serviceName   the service name of who is blocking
      * @param effectiveDate the date at which the entitlement should be paused
      * @param context       the context
      * @return the new <code>Entitlement</code> after the operation was performed
      * @throws EntitlementApiException if the entitlement was not in <tt>ACTIVE</tt> state
     public Entitlement block(String serviceName, final LocalDate effectiveDate, final CallContext context)
             throws EntitlementApiException;
      */
 
     /**
      * Resumes an <code>Entitlement</code> that was paused
      * After this operation, the existing object becomes stale.
      * <p/>
      * The date is interpreted by the system to be in the timezone specified at the <code>Account</code>
      *
      * @param serviceName   the service name of who is blocking
      * @param effectiveDate the date at which the entitlement should be resumed
      * @param context       the context
      * @return the new <code>Entitlement</code> after the operation was performed
      * @throws EntitlementApiException
     public Entitlement unblock(String serviceName, final LocalDate effectiveDate, final CallContext context)
             throws EntitlementApiException;
      */
 }
