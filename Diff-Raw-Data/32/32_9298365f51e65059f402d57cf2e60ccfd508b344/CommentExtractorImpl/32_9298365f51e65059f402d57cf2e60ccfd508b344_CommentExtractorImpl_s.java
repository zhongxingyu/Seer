 package com.bics.jira.mail.handler;
 
 import com.bics.jira.mail.CommentExtractor;
 import com.bics.jira.mail.converter.BodyConverter;
 import com.bics.jira.mail.converter.DefaultHtmlConverter;
 import com.bics.jira.mail.converter.DefaultTextConverter;
 import com.bics.jira.mail.converter.OutlookHtmlConverter;
 import com.bics.jira.mail.converter.StripQuotesOutlookHtmlConverter;
 import com.bics.jira.mail.converter.StripQuotesTextConverter;
 import com.bics.jira.mail.model.HandlerModel;
 import com.bics.jira.mail.model.MessageAdapter;
 import org.apache.commons.lang.StringUtils;
 
 import javax.mail.MessagingException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * Java Doc here
  *
  * @author Victor Polischuk
  * @since 13/02/13 01:32
  */
 public class CommentExtractorImpl implements CommentExtractor {
     private final Collection<? extends BodyConverter> htmlConverters;
     private final Collection<? extends BodyConverter> textConverters;
 
     public CommentExtractorImpl() throws IOException {
        htmlConverters = Arrays.asList(new StripQuotesOutlookHtmlConverter(), new OutlookHtmlConverter(), new DefaultHtmlConverter());
        textConverters = Arrays.asList(new StripQuotesTextConverter(), new DefaultTextConverter());
     }
 
     @Override
     public String extractComment(HandlerModel model, MessageAdapter message) throws MessagingException {
         return extract(model, message, model.isStripQuotes());
     }
 
     @Override
     public String extractBody(HandlerModel model, MessageAdapter message) throws MessagingException {
         return extract(model, message, false);
     }
 
     private String extract(HandlerModel model, MessageAdapter message, boolean stripQuotes) throws MessagingException {
         String text = message.getHtmlTextBody();
 
         if (StringUtils.isBlank(text)) {
             text = message.getPlainTextBody();
 
            return StringUtils.isBlank(text) ? "" : get(model, message, false, textConverters).convert(text);
         }
 
         return get(model, message, stripQuotes, htmlConverters).convert(text);
     }
 
     private BodyConverter get(HandlerModel model, MessageAdapter message, boolean stripQuotes, Collection<? extends BodyConverter> converters) {
         for (BodyConverter converter : converters) {
             if (converter.isSupported(model, message, stripQuotes)) {
                 return converter;
             }
         }
 
         return null;
     }
 }
