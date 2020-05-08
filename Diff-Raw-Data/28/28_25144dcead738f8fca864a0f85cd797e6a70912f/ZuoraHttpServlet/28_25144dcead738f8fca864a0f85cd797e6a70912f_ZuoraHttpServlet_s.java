 package com.ning.killbill.zuora.http;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.ning.billing.payment.api.PaymentMethodPlugin;
 import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
 import com.ning.billing.tenant.api.Tenant;
 import com.ning.billing.util.callcontext.TenantContext;
 import com.ning.killbill.zuora.api.ZuoraPrivateApi;
 import com.ning.killbill.zuora.dao.ZuoraPluginDao;
 import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;
 import com.ning.killbill.zuora.http.PaymentMethodJson.PaymentMethodPluginDetailJson;
 import com.ning.killbill.zuora.osgi.ZuoraActivator;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class ZuoraHttpServlet extends HttpServlet {
 
    private static final String PLUGIN_PATH = "/plugins";
     private static final String REQ_ACCOUNT_ID = "accountId";
     private static final String REQ_PM_ID = "paymentMethodId";
 
 
     private final ZuoraPrivateApi zuoraPrivateApi;
     private final ZuoraPluginDao zuoraPluginDao;
     private final ObjectMapper mapper;
 
     public ZuoraHttpServlet(ZuoraPrivateApi api, ZuoraPluginDao zuoraPluginDao, final ObjectMapper mapper) {
         this.zuoraPrivateApi = api;
         this.zuoraPluginDao = zuoraPluginDao;
         this.mapper = mapper;
     }
 
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         try {
 
             final API api = getAPI(req);
             if (api == null) {
                 resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknow call for " + ZuoraActivator.PLUGIN_NAME);
                 return;
             }
 
             final TenantContext context = createTenantContext(req);
 
             final String accountIdParam = req.getParameter(REQ_ACCOUNT_ID);
             final UUID accountId = UUID.fromString(accountIdParam);
 
             if (api == API.PAYMENT_METHODS) {
 
                 handleGetPaymentMethods(req, resp, accountId, context);
 
             } else if (api == API.PAYMENT_METHOD) {
 
                 final String paymentMethodIdParam = req.getParameter(REQ_PM_ID);
                 final UUID paymentMethodId = UUID.fromString(paymentMethodIdParam);
 
                 handleGetPaymentMethod(req, resp, paymentMethodId, accountId, context);
 
             } else {
                 resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknow call for " + ZuoraActivator.PLUGIN_NAME);
                 return;
             }
         } catch (PaymentPluginApiException e) {
             throw new ServletException("Plugin " + ZuoraActivator.PLUGIN_NAME + " failed : " + e.getMessage(), e);
         }
     }
 
 
     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
 
         try {
 
             final API api = getAPI(req);
             if (api == null || api != API.ACCOUNT) {
                 resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknow call for " + ZuoraActivator.PLUGIN_NAME);
                 return;
             }
 
             final TenantContext context = createTenantContext(req);
 
             final String accountIdParam = req.getParameter(REQ_ACCOUNT_ID);
             final UUID accountId = UUID.fromString(accountIdParam);
 
             handleCreateAccountCreation(req, resp, accountId, context);
 
         } catch (PaymentPluginApiException e) {
             // STEPH
             throw new ServletException("Plugin " + ZuoraActivator.PLUGIN_NAME + " failed : " + e.getMessage(), e);
         }
     }
 
     @Override
     protected void doPut(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
             resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unknow call for " + ZuoraActivator.PLUGIN_NAME);
     }
 
     private void handleCreateAccountCreation(HttpServletRequest req, HttpServletResponse resp, UUID accountId, TenantContext tenantContext)
             throws ServletException, IOException, PaymentPluginApiException {
         final String zuoraAccountId = zuoraPrivateApi.createPaymentProviderAccount(accountId, tenantContext);
        // STEPH
         resp.getOutputStream().write(zuoraAccountId.getBytes());
         resp.setStatus(HttpServletResponse.SC_CREATED);
     }
 
     private void handleGetPaymentMethods(HttpServletRequest req, HttpServletResponse resp, UUID accountId, TenantContext tenantContext)
             throws ServletException, IOException, PaymentPluginApiException {
 
         final List<PaymentMethodEntity> pluginPms = zuoraPluginDao.getPaymentMethods(accountId.toString());
 
         final List<PaymentMethodPlugin> zuoraPms = zuoraPrivateApi.getPaymentMethodDetails(accountId, tenantContext);
 
         final List<PaymentMethodJson> result = new ArrayList<PaymentMethodJson>();
         for (PaymentMethodEntity cur : pluginPms) {
             for (PaymentMethodPlugin z : zuoraPms) {
                 if (z.getExternalPaymentMethodId().equals(cur.getZuoraPaymentMethodId())) {
                     final PaymentMethodPluginDetailJson detail = PaymentMethodPluginDetailJson.toPaymentMethodPluginDetailJson(z.getExternalPaymentMethodId(), z.getProperties());
                     PaymentMethodJson json = new PaymentMethodJson(cur.getKbPaymentMethodId(), cur.getKbAccountId(), cur.isDefault(), ZuoraActivator.PLUGIN_NAME, detail);
                     result.add(json);
                 }
             }
         }
 
         resp.getOutputStream().write(mapper.writeValueAsBytes(result));
         resp.setStatus(HttpServletResponse.SC_OK);
     }
 
 
     private void handleGetPaymentMethod(HttpServletRequest req, HttpServletResponse resp, UUID paymentMethodId, UUID accountId, TenantContext tenantContext)
             throws ServletException, IOException, PaymentPluginApiException {
 
 
         final List<PaymentMethodEntity> pluginPms = zuoraPluginDao.getPaymentMethods(accountId.toString());
 
         final List<PaymentMethodPlugin> zuoraPms = zuoraPrivateApi.getPaymentMethodDetails(accountId, tenantContext);
 
         final List<PaymentMethodJson> result = new ArrayList<PaymentMethodJson>();
         for (PaymentMethodEntity cur : pluginPms) {
 
             if (!cur.getKbPaymentMethodId().equals(paymentMethodId)) {
                 continue;
             }
 
             for (PaymentMethodPlugin z : zuoraPms) {
                 if (z.getExternalPaymentMethodId().equals(cur.getZuoraPaymentMethodId())) {
                     final PaymentMethodPluginDetailJson detail = PaymentMethodPluginDetailJson.toPaymentMethodPluginDetailJson(z.getExternalPaymentMethodId(), z.getProperties());
                     PaymentMethodJson json = new PaymentMethodJson(cur.getKbPaymentMethodId(), cur.getKbAccountId(), cur.isDefault(), ZuoraActivator.PLUGIN_NAME, detail);
                     result.add(json);
                 }
             }
         }
         if (result.size() == 0) {
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         } else {
             resp.getOutputStream().write(mapper.writeValueAsBytes(result.get(0)));
             resp.setStatus(HttpServletResponse.SC_OK);
         }
     }
 
     private void handlePaymentMethodUpdate(HttpServletRequest req, HttpServletResponse resp, UUID paymentMethodId, UUID accountId, TenantContext tenantContext)
             throws ServletException, IOException, PaymentPluginApiException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unimplemented call for " + ZuoraActivator.PLUGIN_NAME);
         return;
     }
 
 
     private API getAPI(final HttpServletRequest req) throws ServletException {
        if ((PLUGIN_PATH + "/" + ZuoraActivator.PLUGIN_NAME + "/" + API.ACCOUNT.getName()).equals(req.getPathInfo())) {
             return API.ACCOUNT;
        } else if ((PLUGIN_PATH + "/" + ZuoraActivator.PLUGIN_NAME + "/" + API.PAYMENT_METHOD.getName()).equals(req.getPathInfo())) {
             return API.PAYMENT_METHOD;
        } else if ((PLUGIN_PATH + "/" + ZuoraActivator.PLUGIN_NAME + "/" + API.PAYMENT_METHODS.getName()).equals(req.getPathInfo())) {
             return API.PAYMENT_METHODS;
         } else {
             return null;
         }
     }
 
     private TenantContext createTenantContext(final ServletRequest request) {
 
         final Tenant tenant = getTenantFromRequest(request);
 
         return new TenantContext() {
             @Override
             public UUID getTenantId() {
                 return tenant != null ? tenant.getId() : null;
             }
         };
     }
 
     private Tenant getTenantFromRequest(final ServletRequest request) {
         final Object tenantObject = request.getAttribute("killbill_tenant");
         if (tenantObject == null) {
             return null;
         } else {
             return (Tenant) tenantObject;
         }
     }
 
     private enum API {
         ACCOUNT("account"),
         PAYMENT_METHOD("paymentMethod"),
         PAYMENT_METHODS("paymentMethods");
 
         String name;
 
         public String getName() {
             return name;
         }
 
         API(String name) {
             this.name = name;
         }
     }
 
     ;
 }
