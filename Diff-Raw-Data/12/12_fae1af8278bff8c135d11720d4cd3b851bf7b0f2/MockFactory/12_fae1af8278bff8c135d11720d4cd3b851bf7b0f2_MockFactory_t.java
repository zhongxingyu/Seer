 package org.wv.stepsovc.web;
 
import org.motechproject.sms.api.service.SmsAuditService;
import org.motechproject.sms.api.service.SmsService;

 import static org.mockito.Mockito.mock;
 
 public class MockFactory {
 
    public static SmsAuditService createAuditService() {
        return mock(SmsAuditService.class);
    }

    public static SmsService createSmsService() {
        return mock(SmsService.class);
     }
 }
