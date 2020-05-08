 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.web;
 
 import java.util.List;
 
 import nl.surfnet.bod.support.ModelStub;
 
 import org.junit.Test;
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.ui.Model;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.collect.Iterables;
 
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class WebUtilsTest {
 
   private final String messageBase = "Test message ";
   private final String message = messageBase + "{}";
   private final String messageArg = "unittest";
   private final String messageArgWithMarkup = WebUtils.PARAM_MARKUP_START + messageArg + WebUtils.PARAM_MARKUP_END;
   private final String[] messageArgs = { messageArg };
   private final String[] emptyArgs = new String[0];
 
   private final MessageSource messageSourceMock = mock(MessageSource.class);
 
   @Test
   public void testSecondPage() {
     assertThat(WebUtils.calculateFirstPage(2), is(WebUtils.MAX_ITEMS_PER_PAGE));
   }
 
   @Test
   public void shouldContainOnePage() {
     assertThat(WebUtils.calculateMaxPages(0), is(1));
     assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE), is(1));
     assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE - 1), is(1));
   }
 
   @Test
   public void shouldContainTwoPages() {
     assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + 1), is(2));
     assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + WebUtils.MAX_ITEMS_PER_PAGE), is(2));
   }
 
   @Test
   public void shouldAddFlashMessage() {
     RedirectAttributes redirectModel = new ModelStub();
 
     when(
         messageSourceMock.getMessage("info_message", new String[] { "<b>een</b>", "<b>twee</b>" },
             LocaleContextHolder.getLocale())).thenReturn("YES");
 
     WebUtils.addInfoFlashMessage(redirectModel, messageSourceMock, "info_message", "een", "twee");
 
     List<String> infoMessages = (List<String>) redirectModel.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY);
 
     assertThat(infoMessages, hasSize(1));
     assertThat(Iterables.getOnlyElement(infoMessages), is("YES"));
   }
 
   @Test
   public void shouldAddMessage() {
     Model model = new ModelStub();
 
     when(
         messageSourceMock.getMessage("info_message", new String[] { "<b>een</b>", "<b>twee</b>" },
             LocaleContextHolder.getLocale())).thenReturn("YES");
 
     WebUtils.addInfoMessage(model, messageSourceMock, "info_message", "een", "twee");
 
     List<String> infoMessages = (List<String>) model.asMap().get(WebUtils.INFO_MESSAGES_KEY);
 
     assertThat(infoMessages, hasSize(1));
     assertThat(Iterables.getOnlyElement(infoMessages), is("YES"));
   }
 
   @Test
   public void shouldShortenAdminGroup() {
     assertThat(WebUtils.shortenAdminGroup("a:b:c:d:e"), is("e"));
     assertThat(WebUtils.shortenAdminGroup("b:c:d:e"), is("e"));
     assertThat(WebUtils.shortenAdminGroup("c:d:e"), is("e"));
     assertThat(WebUtils.shortenAdminGroup("d:e"), is("e"));
     assertThat(WebUtils.shortenAdminGroup("e"), is("e"));
 
     assertThat(WebUtils.shortenAdminGroup(":"), is(""));
     assertThat(WebUtils.shortenAdminGroup(null), nullValue());
     assertThat(WebUtils.shortenAdminGroup(""), is(""));
   }
 
   @Test
   public void shouldMapTeam() {
     assertThat(WebUtils.replaceSearchWith("bladieblateam:hierendaar", "team", "virtualResourceGroup.name"),
         is("bladieblavirtualResourceGroup.name:hierendaar"));
   }
 
   @Test
   public void shouldMapNull() {
     assertThat(WebUtils.replaceSearchWith("bladieblateamhierendaar", null, null), is("bladieblateamhierendaar"));
   }
 }
