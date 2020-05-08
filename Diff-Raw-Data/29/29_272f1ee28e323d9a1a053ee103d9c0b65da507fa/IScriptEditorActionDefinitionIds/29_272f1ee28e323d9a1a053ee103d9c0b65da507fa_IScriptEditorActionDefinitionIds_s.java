 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.actions;
 
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 
 /**
  * Defines the definition IDs for the Script editor actions.
  * 
  * <p>
  * This interface is not intended to be implemented or extended.
  * </p>.
  * 
 	 *
  */
 public interface IScriptEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {
 
 	// edit
 
 	/**
 	 * Action definition ID of the edit -> smart typing action
 	 * 
 	 */
 	public static final String TOGGLE_SMART_TYPING= "org.eclipse.dltk.smartTyping.toggle"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the edit -> go to matching bracket action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.goto.matching.bracket"</code>).
 	 *
 	 *
 	 */
 	public static final String GOTO_MATCHING_BRACKET= "org.eclipse.dltk.ui.edit.text.script.goto.matching.bracket"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the edit -> go to next member action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.goto.next.member"</code>).
 	 *
 	 *
 	 */
 	public static final String GOTO_NEXT_MEMBER= "org.eclipse.dltk.ui.edit.text.script.goto.next.member"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the edit -> go to previous member action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.goto.previous.member"</code>).
 	 *
 	 *
 	 */
 	public static final String GOTO_PREVIOUS_MEMBER= "org.eclipse.dltk.ui.edit.text.script.goto.previous.member"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the edit -> select enclosing action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.select.enclosing"</code>).
 	 */
 	public static final String SELECT_ENCLOSING= "org.eclipse.dltk.ui.edit.text.script.select.enclosing"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the edit -> select next action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.select.next"</code>).
 	 */
 	public static final String SELECT_NEXT= "org.eclipse.dltk.ui.edit.text.script.select.next"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the edit -> select previous action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.select.previous"</code>).
 	 */
 	public static final String SELECT_PREVIOUS= "org.eclipse.dltk.ui.edit.text.script.select.previous"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the edit -> select restore last action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.select.last"</code>).
 	 */
 	public static final String SELECT_LAST= "org.eclipse.dltk.ui.edit.text.script.select.last"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the edit -> content assist complete prefix action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.complete.prefix"</code>).
 	 *
 	 */
 	public static final String CONTENT_ASSIST_COMPLETE_PREFIX= "org.eclipse.dltk.ui.edit.text.script.complete.prefix"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the edit -> show documentation action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.show.documentation"</code>).
 	 */
 	public static final String SHOW_DOCUMENTATION= "org.eclipse.dltk.ui.edit.text.script.show.documentation"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the navigate -> Quick Outline action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.show.outline"</code>).
 	 * 
 	 *
 	 */
 	public static final String SHOW_OUTLINE= "org.eclipse.dltk.ui.edit.text.script.show.outline"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the navigate -> Show Hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.open.hierarchy"</code>).
 	 * 
 	 *
 	 */
 	public static final String OPEN_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.open.hierarchy"; //$NON-NLS-1$
 
 	
 	/**
 	 * Action definition ID of the Navigate -> Open Structure action
 	 * (value <code>"org.eclipse.dltk.ui.navigate.script.open.structure"</code>).
 	 * 
 	 *
 	 */
 	public static final String OPEN_STRUCTURE= "org.eclipse.dltk.ui.navigate.script.open.structure"; //$NON-NLS-1$
 
 	// source
 	
 	/**
 	 * Action definition ID of the source -> comment action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.comment"</code>).
 	 */
 	public static final String COMMENT= "org.eclipse.dltk.ui.edit.text.script.comment"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> uncomment action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.uncomment"</code>).
 	 */
 	public static final String UNCOMMENT= "org.eclipse.dltk.ui.edit.text.script.uncomment"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> toggle comment action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.toggle.comment"</code>).
 	 *
 	 */
 	public static final String TOGGLE_COMMENT= "org.eclipse.dltk.ui.edit.text.script.toggle.comment"; //$NON-NLS-1$
 	
 
 	/**
 	 * Action definition ID of the source -> add block comment action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.add.block.comment"</code>).
 	 *
 	 */
 	public static final String ADD_BLOCK_COMMENT= "org.eclipse.dltk.ui.edit.text.script.add.block.comment"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> remove block comment action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.remove.block.comment"</code>).
 	 *
 	 */
 	public static final String REMOVE_BLOCK_COMMENT= "org.eclipse.dltk.ui.edit.text.script.remove.block.comment"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> indent action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.indent"</code>).
 	 */
 	public static final String INDENT= "org.eclipse.dltk.ui.edit.text.script.indent"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> format action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.format"</code>).
 	 */
 	public static final String FORMAT= "org.eclipse.dltk.ui.edit.text.script.format"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition id of the script quick format action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.quick.format"</code>).
 	 *
 	 */
 	public static final String QUICK_FORMAT= "org.eclipse.dltk.ui.edit.text.script.quick.format"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> add import action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.add.import"</code>).
 	 */
 	public static final String ADD_IMPORT= "org.eclipse.dltk.ui.edit.text.script.add.import"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> organize imports action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.organize.imports"</code>).
 	 */
 	public static final String ORGANIZE_IMPORTS= "org.eclipse.dltk.ui.edit.text.script.organize.imports"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> sort order action (value
 	 * <code>"org.eclipse.dltk.ui.edit.text.script.sort.members"</code>).
 	 *
 	 */
 	public static final String SORT_MEMBERS= "org.eclipse.dltk.ui.edit.text.script.sort.members"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> add Javadoc comment action (value
 	 * <code>"org.eclipse.dltk.ui.edit.text.script.add.Javadoc.comment"</code>).
 	 *
 	 */		
	public static final String ADD_JAVADOC_COMMENT= "org.eclipse.dltk.ui.edit.text.script.add.Javadoc.comment"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> surround with try/catch action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.surround.with.try.catch"</code>).
 	 */
 	public static final String SURROUND_WITH_TRY_CATCH= "org.eclipse.dltk.ui.edit.text.script.surround.with.try.catch"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> override methods action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.override.methods"</code>).
 	 */
 	public static final String OVERRIDE_METHODS= "org.eclipse.dltk.ui.edit.text.script.override.methods"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> add unimplemented constructors action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.add.unimplemented.constructors"</code>).
 	 */
 	public static final String ADD_UNIMPLEMENTED_CONTRUCTORS= "org.eclipse.dltk.ui.edit.text.script.add.unimplemented.constructors"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source ->generate constructor using fields action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.generate.constructor.using.fields"</code>).
 	 */
 	public static final String GENERATE_CONSTRUCTOR_USING_FIELDS= "org.eclipse.dltk.ui.edit.text.script.generate.constructor.using.fields"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source ->generate hashcode() and equals() action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.generate.hashcode.equals"</code>).
 	 *
 	 */
 	public static final String GENERATE_HASHCODE_EQUALS= "org.eclipse.dltk.ui.edit.text.script.generate.hashcode.equals"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the source -> generate setter/getter action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.create.getter.setter"</code>).
 	 */
 	public static final String CREATE_GETTER_SETTER= "org.eclipse.dltk.ui.edit.text.script.create.getter.setter"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> generate delegates action (value
 	 * <code>"org.eclipse.dltk.ui.edit.text.script.create.delegate.methods"</code>).
 	 *
 	 */
 	public static final String CREATE_DELEGATE_METHODS= "org.eclipse.dltk.ui.edit.text.script.create.delegate.methods"; //$NON-NLS-1$
 	
 	
 	/**
 	 * Action definition ID of the source -> externalize strings action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.externalize.strings"</code>).
 	 */
 	public static final String EXTERNALIZE_STRINGS= "org.eclipse.dltk.ui.edit.text.script.externalize.strings"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the source -> find strings to externalize action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.find.strings.to.externalize"</code>).
 	 * 
 	 *
 	 */
 	public static final String FIND_STRINGS_TO_EXTERNALIZE= "org.eclipse.dltk.ui.edit.text.script.find.strings.to.externalize"; //$NON-NLS-1$
 	
 	/**
 	 * Note: this id is for internal use only.
 	 * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#GOTO_NEXT_ANNOTATION}
 	 */
 	public static final String SHOW_NEXT_PROBLEM= "org.eclipse.dltk.ui.edit.text.script.show.next.problem"; //$NON-NLS-1$
 	
 	/**
 	 * Note: this id is for internal use only.
 	 * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#GOTO_PREVIOUS_ANNOTATION}
 	 */
 	public static final String SHOW_PREVIOUS_PROBLEM= "org.eclipse.dltk.ui.edit.text.script.show.previous.problem"; //$NON-NLS-1$
 
 	// refactor
 	
 	/**
 	 * Action definition ID of the refactor -> pull up action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.pull.up"</code>).
 	 */
 	public static final String PULL_UP= "org.eclipse.dltk.ui.edit.text.script.pull.up"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> push down action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.push.down"</code>).
 	 * 
 	 *
 	 */
 	public static final String PUSH_DOWN= "org.eclipse.dltk.ui.edit.text.script.push.down"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> rename element action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.rename.element"</code>).
 	 */
 	public static final String RENAME_ELEMENT= "org.eclipse.dltk.ui.edit.text.script.rename.element"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> modify method parameters action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.modify.method.parameters"</code>).
 	 */
 	public static final String MODIFY_METHOD_PARAMETERS= "org.eclipse.dltk.ui.edit.text.script.modify.method.parameters"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> move element action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.move.element"</code>).
 	 */
 	public static final String MOVE_ELEMENT= "org.eclipse.dltk.ui.edit.text.script.move.element"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> extract local variable action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.extract.local.variable"</code>).
 	 */
 	public static final String EXTRACT_LOCAL_VARIABLE= "org.eclipse.dltk.ui.edit.text.script.extract.local.variable"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> extract constant action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.extract.constant"</code>).
 	 * 
 	 *
 	 */
 	public static final String EXTRACT_CONSTANT= "org.eclipse.dltk.ui.edit.text.script.extract.constant"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> introduce parameter action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.introduce.parameter"</code>).
 	 * 
 	 *
 	 */
 	public static final String INTRODUCE_PARAMETER= "org.eclipse.dltk.ui.edit.text.script.introduce.parameter"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> introduce factory action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.introduce.factory"</code>).
 	 * 
 	 *
 	 */
 	public static final String INTRODUCE_FACTORY= "org.eclipse.dltk.ui.edit.text.script.introduce.factory"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> inline local variable action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.inline.local.variable"</code>).
 	 * @deprecated Use INLINE
 	 */
 	public static final String INLINE_LOCAL_VARIABLE= "org.eclipse.dltk.ui.edit.text.script.inline.local.variable"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> self encapsulate field action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.self.encapsulate.field"</code>).
 	 */
 	public static final String SELF_ENCAPSULATE_FIELD= "org.eclipse.dltk.ui.edit.text.script.self.encapsulate.field"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> extract method action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.extract.method"</code>).
 	 */
 	public static final String EXTRACT_METHOD= "org.eclipse.dltk.ui.edit.text.script.extract.method"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> inline action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.inline"</code>).
 	 *
 	 *
 	 */
 	public static final String INLINE= "org.eclipse.dltk.ui.edit.text.script.inline"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> replace invocations action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.replace.invocations"</code>).
 	 *
 	 *
 	 */
 	public static final String REPLACE_INVOCATIONS= "org.eclipse.dltk.ui.edit.text.script.replace.invocations"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the refactor -> introduce indirection action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.create.indirection"</code>).
 	 *
 	 *
 	 */
 	public static final String INTRODUCE_INDIRECTION= "org.eclipse.dltk.ui.edit.text.script.introduce.indirection"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> extract interface action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.extract.interface"</code>).
 	 * 
 	 *
 	 */
 	public static final String EXTRACT_INTERFACE= "org.eclipse.dltk.ui.edit.text.script.extract.interface"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> change type action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.change.type"</code>).
 	 * 
 	 *
 	 */
 	public static final String CHANGE_TYPE= "org.eclipse.dltk.ui.edit.text.script.change.type"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> move inner type to top level action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.move.inner.to.top.level"</code>).
 	 * 
 	 *
 	 */
 	public static final String MOVE_INNER_TO_TOP= "org.eclipse.dltk.ui.edit.text.script.move.inner.to.top.level"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> use supertype action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.use.supertype"</code>).
 	 * 
 	 *
 	 */
 	public static final String USE_SUPERTYPE= "org.eclipse.dltk.ui.edit.text.script.use.supertype"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> infer generic type arguments action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.infer.type.arguments"</code>).
 	 * 
 	 *
 	 */
 	public static final String INFER_TYPE_ARGUMENTS_ACTION= "org.eclipse.dltk.ui.edit.text.script.infer.type.arguments"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> promote local variable action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.promote.local.variable"</code>).
 	 * 
 	 *
 	 */
 	public static final String PROMOTE_LOCAL_VARIABLE= "org.eclipse.dltk.ui.edit.text.script.promote.local.variable"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the refactor -> convert anonymous to nested action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.convert.anonymous.to.nested"</code>).
 	 * 
 	 *
 	 */
 	public static final String CONVERT_ANONYMOUS_TO_NESTED= "org.eclipse.dltk.ui.edit.text.script.convert.anonymous.to.nested"; //$NON-NLS-1$
 
 	// navigate
 	
 	/**
 	 * Action definition ID of the navigate -> open action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.open.editor"</code>).
 	 */
 	public static final String OPEN_EDITOR= "org.eclipse.dltk.ui.edit.text.script.open.editor"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the navigate -> open super implementation action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.open.super.implementation"</code>).
 	 */
 	public static final String OPEN_SUPER_IMPLEMENTATION= "org.eclipse.dltk.ui.edit.text.script.open.super.implementation"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the navigate -> open external Javadoc action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.open.external.Javadoc"</code>).
 	 */
 	public static final String OPEN_EXTERNAL_JAVADOC= "org.eclipse.dltk.ui.edit.text.script.open.external.Javadoc"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the navigate -> open type hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.org.eclipse.dltk.ui.edit.text.script.open.type.hierarchy"</code>).
 	 */
 	public static final String OPEN_TYPE_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.open.type.hierarchy"; //$NON-NLS-1$
 	
     /**
      * Action definition ID of the navigate -> open call hierarchy action
      * (value <code>"org.eclipse.dltk.ui.edit.text.script.org.eclipse.dltk.ui.edit.text.script.open.call.hierarchy"</code>).
 	 *
      */
     public static final String OPEN_CALL_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.open.call.hierarchy"; //$NON-NLS-1$
     
 	/**
 	 * Action definition ID of the navigate -> show in package explorer action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.show.in.package.view"</code>).
 	 */
 	public static final String SHOW_IN_PACKAGE_VIEW= "org.eclipse.dltk.ui.edit.text.script.show.in.package.view"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the navigate -> show in navigator action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.show.in.navigator.view"</code>).
 	 */
 	public static final String SHOW_IN_NAVIGATOR_VIEW= "org.eclipse.dltk.ui.edit.text.script.show.in.navigator.view"; //$NON-NLS-1$
 
 	// search
 	
 	/**
 	 * Action definition ID of the search -> references in workspace action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.references.in.workspace"</code>).
 	 */
 	public static final String SEARCH_REFERENCES_IN_WORKSPACE= "org.eclipse.dltk.ui.edit.text.script.search.references.in.workspace"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the search -> references in project action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.references.in.project"</code>).
 	 */
 	public static final String SEARCH_REFERENCES_IN_PROJECT= "org.eclipse.dltk.ui.edit.text.script.search.references.in.project"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> references in hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.references.in.hierarchy"</code>).
 	 */
 	public static final String SEARCH_REFERENCES_IN_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.search.references.in.hierarchy"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> references in working set action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.references.in.working.set"</code>).
 	 */
 	public static final String SEARCH_REFERENCES_IN_WORKING_SET= "org.eclipse.dltk.ui.edit.text.script.search.references.in.working.set"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> read access in workspace action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.read.access.in.workspace"</code>).
 	 */
 	public static final String SEARCH_READ_ACCESS_IN_WORKSPACE= "org.eclipse.dltk.ui.edit.text.script.search.read.access.in.workspace"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> read access in project action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.read.access.in.project"</code>).
 	 */
 	public static final String SEARCH_READ_ACCESS_IN_PROJECT= "org.eclipse.dltk.ui.edit.text.script.search.read.access.in.project"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the search -> read access in hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.read.access.in.hierarchy"</code>).
 	 */
 	public static final String SEARCH_READ_ACCESS_IN_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.search.read.access.in.hierarchy"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> read access in working set action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.read.access.in.working.set"</code>).
 	 */
 	public static final String SEARCH_READ_ACCESS_IN_WORKING_SET= "org.eclipse.dltk.ui.edit.text.script.search.read.access.in.working.set"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> write access in workspace action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.write.access.in.workspace"</code>).
 	 */
 	public static final String SEARCH_WRITE_ACCESS_IN_WORKSPACE= "org.eclipse.dltk.ui.edit.text.script.search.write.access.in.workspace"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> write access in project action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.write.access.in.project"</code>).
 	 */
 	public static final String SEARCH_WRITE_ACCESS_IN_PROJECT= "org.eclipse.dltk.ui.edit.text.script.search.write.access.in.project"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> write access in hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.write.access.in.hierarchy"</code>).
 	 */
 	public static final String SEARCH_WRITE_ACCESS_IN_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.search.write.access.in.hierarchy"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> write access in working set action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.write.access.in.working.set"</code>).
 	 */
 	public static final String SEARCH_WRITE_ACCESS_IN_WORKING_SET= "org.eclipse.dltk.ui.edit.text.script.search.write.access.in.working.set"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> declarations in workspace action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.declarations.in.workspace"</code>).
 	 */
 	public static final String SEARCH_DECLARATIONS_IN_WORKSPACE= "org.eclipse.dltk.ui.edit.text.script.search.declarations.in.workspace"; //$NON-NLS-1$
 	/**
 	 * Action definition ID of the search -> declarations in project action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.declarations.in.project"</code>).
 	 */
 	public static final String SEARCH_DECLARATIONS_IN_PROJECTS= "org.eclipse.dltk.ui.edit.text.script.search.declarations.in.project"; //$NON-NLS-1$
 	/**
 	 * Action definition ID of the search -> declarations in hierarchy action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.declarations.in.hierarchy"</code>).
 	 */
 	public static final String SEARCH_DECLARATIONS_IN_HIERARCHY= "org.eclipse.dltk.ui.edit.text.script.search.declarations.in.hierarchy"; //$NON-NLS-1$
 	/**
 	 * Action definition ID of the search -> declarations in working set action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.declarations.in.working.set"</code>).
 	 */
 	public static final String SEARCH_DECLARATIONS_IN_WORKING_SET= "org.eclipse.dltk.ui.edit.text.script.search.declarations.in.working.set"; //$NON-NLS-1$
 	/**
 	 * Action definition ID of the search -> implementors in workspace action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.implementors.in.workspace"</code>).
 	 */
 	public static final String SEARCH_IMPLEMENTORS_IN_WORKSPACE= "org.eclipse.dltk.ui.edit.text.script.search.implementors.in.workspace"; //$NON-NLS-1$
 	/**
 	 * Action definition ID of the search -> implementors in working set action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.implementors.in.working.set"</code>).
 	 */
 	public static final String SEARCH_IMPLEMENTORS_IN_WORKING_SET= "org.eclipse.dltk.ui.edit.text.script.search.implementors.in.working.set"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the search -> implementors in project action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.implementors.in.project"</code>).
 	 *
 	 */
 	public static final String SEARCH_IMPLEMENTORS_IN_PROJECT= "org.eclipse.dltk.ui.edit.text.script.search.implementors.in.project"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> occurrences in file quick menu action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.occurrences.in.file.quickMenu"</code>).
 	 *
 	 */
 	public static final String SEARCH_OCCURRENCES_IN_FILE_QUICK_MENU= "org.eclipse.dltk.ui.edit.text.script.search.occurrences.in.file.quickMenu"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the search -> occurrences in file > elements action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.occurrences.in.file"</code>).
 	 *
 	 */
 	public static final String SEARCH_OCCURRENCES_IN_FILE= "org.eclipse.dltk.ui.edit.text.script.search.occurrences.in.file"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the search -> occurrences in file > exceptions action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.exception.occurrences"</code>).
 	 *
 	 */
 	public static final String SEARCH_EXCEPTION_OCCURRENCES_IN_FILE= "org.eclipse.dltk.ui.edit.text.script.search.exception.occurrences"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the search -> occurrences in file > implements action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.search.implement.occurrences"</code>).
 	 *
 	 */
 	public static final String SEARCH_IMPLEMENT_OCCURRENCES_IN_FILE= "org.eclipse.dltk.ui.edit.text.script.search.implement.occurrences"; //$NON-NLS-1$
 
 	// miscellaneous
 	
 	/**
 	 * Action definition ID of the toggle presentation tool bar button action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.toggle.presentation"</code>).
 	 * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#TOGGLE_SHOW_SELECTED_ELEMENT_ONLY}
 	 */
 	public static final String TOGGLE_PRESENTATION= "org.eclipse.dltk.ui.edit.text.script.toggle.presentation"; //$NON-NLS-1$
 	
 	/**
 	 * Action definition ID of the toggle text hover tool bar button action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.script.toggle.text.hover"</code>).
 	 */
 	public static final String TOGGLE_TEXT_HOVER= "org.eclipse.dltk.ui.edit.text.script.toggle.text.hover"; //$NON-NLS-1$
 
 	/**
 	 * Action definition ID of the remove occurrence annotations action
 	 * (value <code>"org.eclipse.dltk.ui.edit.text.remove.occurrence.annotations"</code>).
 	 *
 	 */
 	public static final String REMOVE_OCCURRENCE_ANNOTATIONS= "org.eclipse.dltk.ui.edit.text.remove.occurrence.annotations";  //$NON-NLS-1$
 
 	/**
 	 * Action definition id of toggle mark occurrences action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.toggleMarkOccurrences"</code>).
 	 *
 	 */
 	public static final String TOGGLE_MARK_OCCURRENCES= "org.eclipse.dltk.ui.edit.text.script.toggleMarkOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * Action definition id of the collapse members action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.folding.collapseMembers"</code>).
 	 *
 	 */
 	public static final String FOLDING_COLLAPSE_MEMBERS= "org.eclipse.dltk.ui.edit.text.script.folding.collapseMembers"; //$NON-NLS-1$
 
 	/**
 	 * Action definition id of the collapse comments action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.folding.collapseComments"</code>).
 	 *
 	 */
 	public static final String FOLDING_COLLAPSE_COMMENTS= "org.eclipse.dltk.ui.edit.text.script.folding.collapseComments"; //$NON-NLS-1$
 
 	/**
 	 * Action definition id of the code clean up action
 	 * (value: <code>"org.eclipse.dltk.ui.edit.text.script.clean.up"</code>).
 	 *
 	 */
 	public static final String CLEAN_UP= "org.eclipse.dltk.ui.edit.text.script.clean.up"; //$NON-NLS-1$
 }
