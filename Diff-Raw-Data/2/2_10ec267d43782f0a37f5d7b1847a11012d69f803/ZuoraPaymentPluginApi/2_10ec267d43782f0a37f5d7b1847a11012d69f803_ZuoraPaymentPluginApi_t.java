 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  *  Ning licenses this file to you under the Apache License, version 2.0
  *  (the "License"); you may not use this file except in compliance with the
  *  License.  You may obtain a copy of the License at:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  */
 
 package com.ning.killbill.zuora.api;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.UUID;
 
 import javax.annotation.Nullable;
 
 import org.joda.time.DateTime;
 import org.osgi.service.log.LogService;
 
 import com.ning.billing.catalog.api.Currency;
 import com.ning.billing.payment.api.PaymentMethodPlugin;
 import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
 import com.ning.billing.payment.plugin.api.PaymentMethodInfoPlugin;
 import com.ning.billing.payment.plugin.api.PaymentPluginApi;
 import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
 import com.ning.billing.payment.plugin.api.RefundInfoPlugin;
 import com.ning.billing.util.callcontext.CallContext;
 import com.ning.billing.util.callcontext.TenantContext;
 import com.ning.killbill.zuora.dao.ZuoraPluginDao;
 import com.ning.killbill.zuora.dao.entities.PaymentEntity;
 import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;
 import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
 import com.ning.killbill.zuora.util.Either;
 import com.ning.killbill.zuora.zuora.ConnectionPool;
 import com.ning.killbill.zuora.zuora.PaymentConverter;
 import com.ning.killbill.zuora.zuora.PaymentMethodConverter;
 import com.ning.killbill.zuora.zuora.PaymentMethodInfoConverter;
 import com.ning.killbill.zuora.zuora.ZuoraApi;
 import com.ning.killbill.zuora.zuora.ZuoraConnection;
 import com.ning.killbill.zuora.zuora.ZuoraError;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.zuora.api.object.Payment;
 import com.zuora.api.object.PaymentMethod;
 
 public class ZuoraPaymentPluginApi extends ZuoraApiBase implements PaymentPluginApi {
 
 
     public ZuoraPaymentPluginApi(final ConnectionPool pool, final ZuoraApi api, final LogService logService, final DefaultKillbillApi defaultKillbillApi,
                                  final ZuoraPluginDao zuoraPluginDao, final String instanceName) {
         super(pool, api, logService, defaultKillbillApi, zuoraPluginDao, instanceName);
     }
 
 
     @Override
     public PaymentInfoPlugin processPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
 
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
         final Either<ZuoraError, PaymentInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentInfoPlugin>>() {
             @Override
             public Either<ZuoraError, PaymentInfoPlugin> withConnection(final ZuoraConnection connection) {
 
                 Either<ZuoraError, Payment> rowPaymentOrError = zuoraApi.processPayment(connection, accountExternalKey, amount, kbPaymentId.toString());
                 if (rowPaymentOrError.isRight()) {
                     final Payment rowPayment = rowPaymentOrError.getRight();
                     zuoraPluginDao.insertPayment(new PaymentEntity(kbPaymentId.toString(),
                                                                    kbAccountId.toString(),
                                                                    rowPayment.getId(),
                                                                    rowPayment.getCreatedDate().toDate(),
                                                                    rowPayment.getEffectiveDate().toDate(),
                                                                    rowPayment.getAmount(),
                                                                    rowPayment.getStatus(),
                                                                    rowPayment.getGatewayResponse(),
                                                                    rowPayment.getGatewayResponseCode(),
                                                                    rowPayment.getReferenceId(),
                                                                    rowPayment.getSecondPaymentReferenceId()));
                 }
                 return convert(rowPaymentOrError, errorConverter, paymentConverter);
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             return result.getRight();
         }
     }
 
     @Override
     public PaymentInfoPlugin getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId, final TenantContext context) throws PaymentPluginApiException {
 
         final PaymentEntity res = zuoraPluginDao.getPayment(kbPaymentId.toString());
         if (res != null) {
             return new PaymentInfoPlugin() {
                 @Override
                 public BigDecimal getAmount() {
                     return res.getAmount();
                 }
                 @Override
                 public DateTime getCreatedDate() {
                     return new DateTime(res.getCreatedDate());
                 }
                 @Override
                 public DateTime getEffectiveDate() {
                     return new DateTime(res.getEffectiveDate());
                 }
                 @Override
                 public PaymentPluginStatus getStatus() {
                     return PaymentConverter.toPluginStatus(res.getStatus());
                 }
                 @Override
                 public String getGatewayError() {
                     return res.getGatewayError();
                 }
                 @Override
                 public String getGatewayErrorCode() {
                     return res.getGatewayErrorCode();
                 }
                 @Override
                 public String getFirstPaymentReferenceId() {
                     return res.getReferenceId();
                 }
                 @Override
                 public String getSecondPaymentReferenceId() {
                     return res.getSecondReferenceId();
                 }
             };
         }
 
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(kbAccountId, context);
         final Either<ZuoraError, Payment> result = withConnection(new ConnectionCallback<Either<ZuoraError, Payment>>() {
 
             @Override
             public Either<ZuoraError, Payment> withConnection(final ZuoraConnection connection) {
                 return zuoraApi.getPaymentForKillbillPayment(connection, accountExternalKey, kbPaymentId.toString());
             }
         });
 
         if (result.isRight()) {
             final Payment rowPayment = result.getRight();
             zuoraPluginDao.insertPayment(new PaymentEntity(kbPaymentId.toString(),
                                                            kbAccountId.toString(),
                                                            rowPayment.getId(),
                                                            rowPayment.getCreatedDate().toDate(),
                                                            rowPayment.getEffectiveDate().toDate(),
                                                            rowPayment.getAmount(),
                                                            rowPayment.getStatus(),
                                                            rowPayment.getGatewayResponse(),
                                                            rowPayment.getGatewayResponseCode(),
                                                            rowPayment.getReferenceId(),
                                                            rowPayment.getSecondPaymentReferenceId()));
         }
 
         final Either<ZuoraError, PaymentInfoPlugin> finalResult =  convert(result, errorConverter, paymentConverter);
         if (finalResult.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             return finalResult.getRight();
         }
     }
 
     @Override
     public RefundInfoPlugin processRefund(final UUID kbAccountId, final UUID kbPaymentId, final BigDecimal refundAmount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
 
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromPaymentId(kbPaymentId, context);
         final Either<ZuoraError, RefundInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, RefundInfoPlugin>>() {
             @Override
             public Either<ZuoraError, RefundInfoPlugin> withConnection(final ZuoraConnection connection) {
                 final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, accountExternalKey);
                 if (accountOrError.isLeft()) {
                     return Either.left(accountOrError.getLeft());
                 }
                 final String accountId = accountOrError.getRight().getId();
 
                 final Either<ZuoraError, List<com.zuora.api.object.Payment>> paymentsOrError = zuoraApi.getProcessedPaymentsForAccount(connection, accountId);
                 if (paymentsOrError.isLeft()) {
                     return Either.left(paymentsOrError.getLeft());
                 }
                 com.zuora.api.object.Payment paymentToBeRefunded = null;
                 for (final com.zuora.api.object.Payment cur : paymentsOrError.getRight()) {
 
                     if (cur.getComment() != null && cur.getComment().equals(kbPaymentId.toString())) {
                         paymentToBeRefunded = cur;
                         break;
                     }
                 }
                 if (paymentToBeRefunded == null) {
                     return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "Can't find Payment object for refund"));
                 }
                 return convert(zuoraApi.createRefund(connection, paymentToBeRefunded.getId(), kbPaymentId.toString(), refundAmount), errorConverter, refundConverter);
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             return result.getRight();
         }
     }
 
     @Override
    public List<RefundInfoPlugin> getRefundInfo(final UUID uuid, final UUID uuid2, final TenantContext callContext) throws PaymentPluginApiException {
         return null;
     }
 
     @Override
     public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps,
                                  final boolean setDefault, final CallContext context) throws PaymentPluginApiException {
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(kbAccountId, context);
         final Either<ZuoraError, PaymentMethodPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentMethodPlugin>>() {
             @Override
             public Either<ZuoraError, PaymentMethodPlugin> withConnection(final ZuoraConnection connection) {
                 final Either<ZuoraError, PaymentMethod> paymentMethodOrError = zuoraApi.addPaymentMethod(connection, accountExternalKey, paymentMethodProps, setDefault);
                 if (paymentMethodOrError.isLeft()) {
                     return convert(paymentMethodOrError, errorConverter, null);
                 } else {
                     final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getAccountById(connection, paymentMethodOrError.getRight().getAccountId());
 
                     if (accountOrError.isLeft()) {
                         return convert(accountOrError, errorConverter, null);
                     } else {
                         final com.zuora.api.object.Account account = accountOrError.getRight();
                         final PaymentMethodConverter converter = new PaymentMethodConverter(account);
                         return convert(paymentMethodOrError, errorConverter, converter);
                     }
                 }
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             final PaymentMethodPlugin paymentMethodPlugin = result.getRight();
             final PaymentMethodEntity paymentMethodEntity = new PaymentMethodEntity(kbPaymentMethodId.toString(), kbAccountId.toString(), paymentMethodPlugin.getExternalPaymentMethodId(), paymentMethodPlugin.isDefaultPaymentMethod());
             zuoraPluginDao.insertPaymentMethod(paymentMethodEntity);
         }
     }
 
     @Override
     public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
 
         final PaymentMethodEntity entity = zuoraPluginDao.getPaymentMethodById(kbPaymentMethodId.toString());
         if (entity == null) {
             // Ignore, there is nothing to delete on zuora side, looks like...
             return;
         }
         final String zuoraPaymentMethodId = entity.getZuoraPaymentMethodId();
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
         final Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
             @Override
             public Either<ZuoraError, Void> withConnection(final ZuoraConnection connection) {
                 return convert(zuoraApi.deletePaymentMethod(connection, accountExternalKey, zuoraPaymentMethodId), errorConverter, null);
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             zuoraPluginDao.deletePaymentMethodById(kbPaymentMethodId.toString());
         }
     }
 
     @Override
     public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId, final UUID kbPaymentMethodId, final TenantContext context) throws PaymentPluginApiException {
         final PaymentMethodEntity paymentMethodEntity = zuoraPluginDao.getPaymentMethodById(kbPaymentMethodId.toString());
         if (paymentMethodEntity == null) {
             return null;
         }
 
         final Either<ZuoraError, PaymentMethodPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentMethodPlugin>>() {
             @Override
             public Either<ZuoraError, PaymentMethodPlugin> withConnection(final ZuoraConnection connection) {
 
                 final Either<ZuoraError, PaymentMethod> paymentMethodOrError = zuoraApi.getPaymentMethodById(connection, paymentMethodEntity.getZuoraPaymentMethodId());
                 if (paymentMethodOrError.isLeft()) {
                     return convert(paymentMethodOrError, errorConverter, null);
                 } else {
                     final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getAccountById(connection, paymentMethodOrError.getRight().getAccountId());
 
                     if (accountOrError.isLeft()) {
                         return convert(accountOrError, errorConverter, null);
                     } else {
                         final com.zuora.api.object.Account account = accountOrError.getRight();
                         final PaymentMethodConverter converter = new PaymentMethodConverter(account);
                         return convert(paymentMethodOrError, errorConverter, converter);
                     }
                 }
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             return result.getRight();
         }
     }
 
     @Override
     public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
         final PaymentMethodEntity paymentMethodEntity = zuoraPluginDao.getPaymentMethodById(kbPaymentMethodId.toString());
         if (paymentMethodEntity == null) {
             return;
         }
 
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
         final Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
             @Override
             public Either<ZuoraError, Void> withConnection(final ZuoraConnection connection) {
 
 
                 final Either<ZuoraError, PaymentMethod> paymentMethodOrError = zuoraApi.getPaymentMethodById(connection, paymentMethodEntity.getZuoraPaymentMethodId());
                 if (paymentMethodOrError.isLeft()) {
                     return convert(paymentMethodOrError, errorConverter, null);
                 }
                 final PaymentMethod paymentMethod = paymentMethodOrError.getRight();
 
                 final Either<ZuoraError, Void> updatePaymentOrError = zuoraApi.setDefaultPaymentMethod(connection, accountExternalKey, paymentMethod);
                 return convert(updatePaymentOrError, errorConverter, null);
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             for (final PaymentMethodEntity entityForAccount : zuoraPluginDao.getPaymentMethods(paymentMethodEntity.getKbAccountId())) {
                 entityForAccount.setDefault(entityForAccount.getKbPaymentMethodId().equals(kbPaymentMethodId.toString()));
                 zuoraPluginDao.updatePaymentMethod(entityForAccount);
             }
         }
     }
 
     @Override
     public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final CallContext context) throws PaymentPluginApiException {
 
         final String accountExternalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(kbAccountId, context);
         final Either<ZuoraError, List<PaymentMethodInfoPlugin>> result = withConnection(new ConnectionCallback<Either<ZuoraError, List<PaymentMethodInfoPlugin>>>() {
             @Override
             public Either<ZuoraError, List<PaymentMethodInfoPlugin>> withConnection(final ZuoraConnection connection) {
                 final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, accountExternalKey);
 
                 if (accountOrError.isLeft()) {
                     return convert(accountOrError, errorConverter, null);
                 } else {
 
                     final List<PaymentMethodEntity> pms = zuoraPluginDao.getPaymentMethods(kbAccountId.toString());
                     final PaymentMethodInfoConverter converter = new PaymentMethodInfoConverter(kbAccountId, accountOrError.getRight().getDefaultPaymentMethodId(), pms);
 
                     final com.zuora.api.object.Account account = accountOrError.getRight();
                     final Either<ZuoraError, List<PaymentMethod>> paymentMethodsOrError = zuoraApi.getPaymentMethodsForAccount(connection, account);
 
                     return convertList(paymentMethodsOrError, errorConverter, converter);
                 }
             }
         });
         if (result.isLeft()) {
             throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
         } else {
             return result.getRight();
         }
 
     }
 
     @Override
     public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods) throws PaymentPluginApiException {
 
         final List<PaymentMethodEntity> restInput = ImmutableList.<PaymentMethodEntity>copyOf(Collections2.transform(paymentMethods, new Function<PaymentMethodInfoPlugin, PaymentMethodEntity>() {
             @Override
             public PaymentMethodEntity apply(@Nullable final PaymentMethodInfoPlugin input) {
                 return new PaymentMethodEntity(input.getPaymentMethodId().toString(), input.getAccountId().toString(), input.getExternalPaymentMethodId(), input.isDefault());
             }
         }));
         zuoraPluginDao.resetPaymentMethods(restInput);
     }
 }
