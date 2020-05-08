 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: FillTemplateChain.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Sets up the chain to provide all placeholder replacements in templates.
  *
  * Date: 6/3/12
  * Time: 12:34 PM
  *
  */
 package org.acmsl.queryj.templates;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.AbstractQueryJChain;
 import org.acmsl.queryj.QueryJCommand;
 import org.acmsl.queryj.templates.handlers.fillhandlers.DAOChooserPropertiesFileNameHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.JndiLocationFillHandler;
 import org.acmsl.queryj.tools.QueryJBuildException;
 import org.acmsl.queryj.templates.handlers.FillAdapterHandler;
 import org.acmsl.queryj.templates.handlers.TemplateContextFillAdapterHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.CopyrightYearsHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.CurrentYearHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.DAOSubpackageNameHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.DatabaseEngineNameHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.DatabaseEngineVersionHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.DecoratedString;
 import org.acmsl.queryj.templates.handlers.fillhandlers.HeaderHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.IsRepositoryDAOHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.ProjectPackageHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.RepositoryNameHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.SerialVersionUIDHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.SubPackageNameHandler;
 import org.acmsl.queryj.templates.handlers.fillhandlers.TimestampHandler;
 
 /*
  * Importing some ACM-SL Commons classes.
  */
 import org.acmsl.commons.patterns.Chain;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing some JDK classes.
  */
 import java.util.Map;
 
 /**
  * Sets up the chain to provide all placeholder replacements in templates.
  * @author <a href="mailto:chous@acm-sl.org">chous</a>
  * @since 2012/06/03
  */
 public abstract class FillTemplateChain<C extends TemplateContext>
     extends AbstractQueryJChain
 {
     /**
      * The template context.
      */
     private C templateContext;
 
     /**
      * Whether to include only relevant placeholders.
      */
     private boolean relevantOnly;
 
     /**
      * Creates a new {@link FillTemplateChain} associated to given
      * {@link TemplateContext}.
      * @param context the template.
      * @param relevantOnly whether to include only relevant placeholders.
      */
     protected FillTemplateChain(@NotNull final C context, final boolean relevantOnly)
     {
         immutableSetTemplateContext(context);
         immutableSetRelevantOnly(relevantOnly);
     }
 
     /**
      * Specifies the template context.
      * @param context the context.
      */
     protected final void immutableSetTemplateContext(@NotNull final C context)
     {
         this.templateContext = context;
     }
 
     /**
      * Specifies the template context.
      * @param context the context.
      */
     @SuppressWarnings("unused")
     protected void setTemplateContext(@NotNull final C context)
     {
         immutableSetTemplateContext(context);
     }
 
     /**
      * Retrieves the template context.
      * @return such information.
      */
     @NotNull
     public C getTemplateContext()
     {
         return templateContext;
     }
 
     /**
      * Specifies whether to include only relevant placeholders.
      * @param relevantOnly such condition.
      */
     protected final void immutableSetRelevantOnly(final boolean relevantOnly)
     {
         this.relevantOnly = relevantOnly;
     }
 
     /**
      * Specifies whether to include only relevant placeholders.
      * @param relevantOnly such condition.
      */
     @SuppressWarnings("unused")
     protected void setRelevantOnly(final boolean relevantOnly)
     {
         this.relevantOnly = relevantOnly;
     }
 
     /**
      * Retrieves whether to include only relevant placeholders.
      * @return such condition.
      */
     protected boolean getRelevantOnly()
     {
         return this.relevantOnly;
     }
 
     /**
      * Adds additional handlers.
      * @param chain the chain.
      * @param context the {@link TemplateContext context}.
      * @param relevantOnly whether to include only relevant placeholders or not.
      */
     protected abstract void addHandlers(
         @NotNull final Chain chain, @NotNull final C context, final boolean relevantOnly);
 
     /**
      * Builds the command.
      *
      * @param command the command to be initialized.
      * @return the initialized command.
      */
     @NotNull
     @Override
     protected QueryJCommand buildCommand(final QueryJCommand command)
     {
         return command;
     }
 
     /**
      * Performs any clean up whenever an error occurs.
      * @param buildException the error that triggers this clean-up.
      * @param command the command.
      */
     @Override
     protected void cleanUpOnError(final QueryJBuildException buildException, final QueryJCommand command)
     {
         // nothing required.
     }
 
     /**
      * Performs the required processing.
      * @throws QueryJBuildException if the process fails.
      */
     @SuppressWarnings("unchecked")
     public Map providePlaceholders()
         throws QueryJBuildException
     {
         Map result;
 
         @NotNull QueryJCommand t_Command = buildCommand();
 
         super.process(buildChain(getChain()), t_Command);
 
         result = t_Command.getAttributeMap();
 
         return result;
     }
 
     /**
      * Builds the chain.
      *
      * @param chain the chain to be configured.
      * @return the updated chain.
      */
     @Override
     @NotNull
     protected final Chain buildChain(@NotNull final Chain chain)
     {
         return buildChain(chain, getTemplateContext(), getRelevantOnly());
     }
 
     /**
      * Adds given handler depending on whether it's relevant or not.
      * @param chain the chain.
      * @param handler the handler.
      * @param relevantOnly whether to include only relevant placeholders.
      */
     protected void add(
         @NotNull final Chain chain,
         @NotNull final FillAdapterHandler handler,
         final boolean relevantOnly)
     {
         FillAdapterHandler actualHander = handler;
 
         if (   (relevantOnly)
            && (handler.getFillHandler() instanceof NonRelevantFillHandler))
         {
             actualHander = new EmptyFillAdapterHandler(handler.getFillHandler());
         }
 
         chain.add(actualHander);
     }
 
     /**
      * Builds the chain.
      *
      * @param chain the chain to be configured.
      * @param context the context.
      * @param relevantOnly whether to include only relevant placeholders.
      * @return the updated chain.
      */
     @NotNull
     protected final Chain buildChain(
         @NotNull final Chain chain, @NotNull final C context, final boolean relevantOnly)
     {
         add(
             chain,
             new FillAdapterHandler<CopyrightYearsHandler,Integer[]>(new CopyrightYearsHandler()),
             relevantOnly);
 
         add(
             chain,
             new FillAdapterHandler<CurrentYearHandler,String>(new CurrentYearHandler()),
             relevantOnly);
 
         add(
             chain,
             new FillAdapterHandler<TimestampHandler,String>(new TimestampHandler()),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,DAOSubpackageNameHandler,String>(
                 new DAOSubpackageNameHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,DatabaseEngineNameHandler,DecoratedString>(
                 new DatabaseEngineNameHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,DatabaseEngineVersionHandler,DecoratedString>(
                 new DatabaseEngineVersionHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,HeaderHandler,DecoratedString>(
                 new HeaderHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,IsRepositoryDAOHandler,Boolean>(
                 new IsRepositoryDAOHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,ProjectPackageHandler,DecoratedString>(
                 new ProjectPackageHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,RepositoryNameHandler,DecoratedString>(
                 new RepositoryNameHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,SubPackageNameHandler,DecoratedString>(
                 new SubPackageNameHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,SerialVersionUIDHandler,Long>(
                 new SerialVersionUIDHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,JndiLocationFillHandler,DecoratedString>(
                 new JndiLocationFillHandler(context)),
             relevantOnly);
 
         add(
             chain,
             new TemplateContextFillAdapterHandler<TemplateContext,DAOChooserPropertiesFileNameHandler,DecoratedString>(
                 new DAOChooserPropertiesFileNameHandler(context)),
             relevantOnly);
 
         addHandlers(chain, context, relevantOnly);
 
         return chain;
     }
 }
