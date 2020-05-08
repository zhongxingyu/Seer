 package com.oreilly.common.interaction.text;
 
 import java.util.HashMap;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.oreilly.common.interaction.text.error.AbortInteraction;
 import com.oreilly.common.interaction.text.error.ContextDataRequired;
 import com.oreilly.common.interaction.text.error.GeneralInteractionError;
 import com.oreilly.common.interaction.text.formatter.Formatter;
 import com.oreilly.common.interaction.text.helpers.Choices;
 import com.oreilly.common.interaction.text.helpers.PaginationAssistant;
 import com.oreilly.common.interaction.text.validator.Validator;
 import com.oreilly.common.text.VariableTool;
 
 
 // TODO: "Loopback" method, which pops this page back into the front of the queue
 // TODO: "Bookmark" method, which adds this page to the bookmark stack (for when player uses "back" command)
 
 abstract public class InteractionPage {
 	
 	static public final int MAX_LINES = 20;
 	
 	public Formatter formatter = null;
 	public Validator validator = null;
 	public String validationFailedMessage = null;
 	public HashMap< String, Object > style = new HashMap< String, Object >();
 	
 	// translation support
 	public String translationKey = null;
 	public boolean translatableBody = false;
 	protected boolean translatableTitle = false;
 	
 	// title 
 	public boolean hasTitle = false;
 	protected String defaultTitle = "Title";
 	
 	// choices
 	public boolean hasChoices = false;
 	
 	
 	// variables
 	
 	public HashMap< String, Object > getVariables( Interaction interaction ) {
 		return null;
 	}
 	
 	// pagination
 	
 	public int maxLines = 20;
 	
 	
 	// convience function
 	
 	public void autoPage() {
 		translatableBody = true;
 		hasTitle = true;
 		translatableTitle = true;
 	}
 	
 	
 	protected void autoTitle() {
 		hasTitle = true;
 		translatableTitle = true;
 	}
 	
 	
 	// translations
 	
 	public String getTranslationKey() {
 		if ( translationKey != null )
 			return translationKey;
 		else {
 			String[] split = this.getClass().getName().split("\\.");
 			return split[ split.length - 1 ];
 		}
 	}
 	
 	
 	// for times when the entire message may change depending on context
 	public String getCustomTranslationKey( Interaction interaction ) {
 		return null;
 	}
 	
 	
 	// Title functions
 	
 	public String titleOverrideKey() {
 		return getTranslationKey() + ".titleOverride";
 	}
 	
 	
 	public String getTitle( Interaction interaction ) {
 		Object overrideObj = interaction.context.get( titleOverrideKey() );
 		if ( overrideObj != null )
 			return overrideObj.toString();
 		else {
 			if ( translatableTitle )
 				return VariableTool.variable( getTranslationKey() + ".title" );
 			else
 				return defaultTitle;
 		}
 	}
 	
 	
 	public String getDisplayText( Interaction interaction ) throws ContextDataRequired,
 			GeneralInteractionError, AbortInteraction {
 		// TOOD: Pagination support...
 		// see if we have an active paginator
 		PaginationAssistant paginator = interaction.getContextData(
 				PaginationAssistant.class, interaction, getTranslationKey() + "_paginator" );
 		if ( paginator != null ) {
 			return paginator.getDisplayText();
 		} else {
 			// generate page content, and if there's too much, then activate a paginator..
 			if ( hasChoices ) {
 				Choices choices = Choices.getChoices( this, interaction );
 				if ( choices == null )
 					choices = generateChoices( interaction );
 				String text = generateDisplayBody( interaction ) + "\n";
 				String choiceText = interaction.parseMessage(
						StringUtils.join( choices.generateChoiceList(), "\n" ) );
				if ( text.split( "\n" ).length + choiceText.split( "\n" ).length > maxLines ) {
 					// we'll need a paginator
 					paginator = new PaginationAssistant( choiceText, maxLines, text );
 					interaction.context.put( getTranslationKey() + "_paginator", paginator );
 					return paginator.getDisplayText();
 				} else
					return text + choiceText;
 			} else {
 				String text = generateDisplayBody( interaction );
 				if ( text.split( "\n" ).length > maxLines ) {
 					paginator = new PaginationAssistant( text, maxLines, "" );
 					interaction.context.put( getTranslationKey() + "_paginator", paginator );
 					return paginator.getDisplayText();
 				} else
 					return text;
 			}
 		}
 	}
 	
 	
 	public Choices generateChoices( Interaction interaction ) throws ContextDataRequired,
 			GeneralInteractionError, AbortInteraction {
 		return null;
 	}
 	
 	
 	public String generateDisplayBody( Interaction interaction ) {
 		String text = null;
 		if ( translatableBody ) {
 			String customKey = getCustomTranslationKey( interaction );
 			if ( customKey == null )
 				text = VariableTool.variable( getTranslationKey() + ".text" );
 			else
 				text = customKey;
 		}
 		else
 			text = getText();
 		return interaction.parseMessage( text );
 	}
 	
 	
 	public String getText() {
 		return "";
 	}
 	
 	
 	public String inputHandler( Interaction interaction, Object data ) throws ContextDataRequired,
 			GeneralInteractionError, AbortInteraction {
 		// first, check for pagination...
 		PaginationAssistant paginator = interaction.getContextData(
 				PaginationAssistant.class, interaction, getTranslationKey() + "_paginator" );
 		if ( paginator != null ) {
 			boolean paginationCommand = paginator.processPageCommand( data.toString() );
 			if ( paginationCommand ) {
 				interaction.pageWaitingForInput = true;
 				return null;
 			} else {
 				// it wasn't a pagination command, so we kill the paginator
 				// so we don't end up with a cache of old data if the player
 				// comes back to this interaction page
 				interaction.context.remove( getTranslationKey() + "_paginator" );
 			}
 		}
 		// if we don't have pagination (or it wasn't a pagination command..)
 		//  see if we are using a choice model, or straight to direct input
 		if ( hasChoices ) {
 			Choices choices = Choices.getChoices( this, interaction );
 			return choices.takeAction( this, interaction, data );
 		} else
 			return acceptValidatedInput( interaction, data );
 	}
 	
 	
 	// Direct Input model:
 	// if a string is returned, it is shown to the player
 	public String acceptValidatedInput( Interaction interaction, Object data ) throws ContextDataRequired,
 			GeneralInteractionError, AbortInteraction {
 		return null;
 	}
 	
 	
 	// Choice model:
 	// as above, a string return is shown to the player
 	public String takeAction( Interaction interaction, String key ) throws ContextDataRequired,
 			GeneralInteractionError, AbortInteraction {
 		return null;
 	}
 	
 	
 	// chained init methods
 	
 	public InteractionPage withFormatter( Formatter formatter ) {
 		if ( this.formatter == null )
 			this.formatter = formatter;
 		else
 			this.formatter.chain( formatter );
 		return this;
 	}
 	
 	
 	public InteractionPage withValidator( Validator validator ) {
 		if ( this.validator == null )
 			this.validator = validator;
 		else
 			this.validator.chain( validator );
 		return this;
 	}
 	
 	
 	public InteractionPage withReplacementFormatter( Formatter formatter ) {
 		this.formatter = formatter;
 		return this;
 	}
 	
 	
 	public InteractionPage withReplacementValidator( Validator validator ) {
 		this.validator = validator;
 		return this;
 	}
 	
 	
 	public InteractionPage withValidationFailedMessage( String message ) {
 		this.validationFailedMessage = message;
 		return this;
 	}
 	
 	
 	public InteractionPage withStyle( String key, Object style ) {
 		this.style.put( key, style );
 		return this;
 	}
 	
 	
 	public InteractionPage withStyles( HashMap< String, Object > source ) {
 		style.putAll( source );
 		return this;
 	}
 	
 }
