 /**
  * 
  */
 package org.dotplot.tokenizer.filter.ui;
 
 import java.util.Map;
 import java.util.Vector;
 
 import org.dotplot.tokenizer.filter.IFilterConfiguration;
 import org.dotplot.tokenizer.filter.SentenceFilter;
 import org.dotplot.tokenizer.filter.stemmingFilter.StemmerLanguage;
 import org.dotplot.ui.ConfigurationView;
 import org.dotplot.ui.ViewController;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * @author Nils Braden <nils.braden@mni.fh-giessen.de>
  * 
  */
 public class StemmerFilterUI extends AbstractFilterUI {
 
     public static final String INFO = "This Filter can be used to have stemming applied to the text before plotting";
 
    private ConfigurationView view;

     private Button stemmerCheckBox;
 
     private Combo selectStemmerCombo;
     private Combo selectLanguageCombo;
 
     public StemmerFilterUI() {
 	super("Stemmer", INFO);
     }
 
     /**
      * @param title
      * @param info
      */
     public StemmerFilterUI(String title, String info) {
 	super(title, info);
     }
 
     /**
      * 
      * @see org.dotplot.tokenizer.filter.ui.AbstractFilterUI#drawContent(org.eclipse
      *      .swt.widgets.Composite, org.eclipse.swt.events.SelectionListener)
      */
     @Override
     public Control drawContent(Composite parent,
 	    SelectionListener changeListener) {
 	Composite c = new Composite(parent, SWT.NONE);
 	RowLayout layout = new RowLayout();
 	c.setLayout(layout);
 
 	// Button zum aktivieren des Stemmers
 	final Button bSentences = new Button(c, SWT.CHECK);
 	bSentences.setText("stem words");
 	bSentences.setToolTipText("activate the stemmer filter");
 	this.stemmerCheckBox = bSentences;
 
 	// SelectStemmer Dropdown Menu
 	final Combo cSelectStemmer = new Combo(c, SWT.READ_ONLY | SWT.DROP_DOWN);
 	this.selectStemmerCombo = cSelectStemmer;
 	cSelectStemmer.setToolTipText("choose a stemmer");
 
 	// SelectLanguage Dropdown Menu
 	final Combo cSelectLang = new Combo(c, SWT.READ_ONLY | SWT.DROP_DOWN);
 	this.selectLanguageCombo = cSelectLang;
 	cSelectLang.setToolTipText("choose a Language");
 
 	// Listen auf die Checkbox
 	SelectionListener filterListener = new SelectionAdapter() {
 	    @Override
 	    public void widgetSelected(SelectionEvent e) {
 		Button source = (Button) e.getSource();
 		if (source.getSelection()) {
 		    cSelectStemmer.setEnabled(true);
 		    cSelectLang.setEnabled(true);
 		} else {
 		    cSelectStemmer.setEnabled(false);
 		    cSelectStemmer.clearSelection();
 		    cSelectStemmer.deselectAll();
 		    cSelectLang.setEnabled(false);
 		    cSelectLang.clearSelection();
 		    cSelectLang.deselectAll();
 		}
 	    }
 	};
 
 	// SelectStemmer Dropdown Menu füllen
 	String[] stemmers = getAvailableStemmers();
 	if (stemmers.length > 0) {
 	    bSentences.addSelectionListener(changeListener);
 	    bSentences.addSelectionListener(filterListener);
 	    cSelectStemmer.setItems(stemmers);
 	    cSelectStemmer.addSelectionListener(changeListener);
 	} else {
 	    bSentences.setEnabled(false);
 	    cSelectStemmer.setEnabled(false);
 	}
 
 	// SelectLanguage Dropdown Menu füllen
 	String[] languages = getAvailableLanguagesStrings();
 	if (languages.length > 0) {
 	    bSentences.addSelectionListener(changeListener);
 	    bSentences.addSelectionListener(filterListener);
 	    cSelectLang.setItems(languages);
 	    cSelectLang.addSelectionListener(changeListener);
 	} else {
 	    bSentences.setEnabled(false);
 	    cSelectLang.setEnabled(false);
 	}
 
 	return null;
     }
 
     StemmerLanguage[] getAvailableLanguages() {
 	Vector<StemmerLanguage> v = new Vector<StemmerLanguage>();
 	v.add(StemmerLanguage.DE);
 	v.add(StemmerLanguage.EN);
 	v.add(StemmerLanguage.FR);
 	return v.toArray(new StemmerLanguage[0]);
     }
 
     String[] getAvailableLanguagesStrings() {
 	StemmerLanguage[] languages = getAvailableLanguages();
 	Vector<String> v = new Vector<String>();
 	for (StemmerLanguage sl : languages) {
 	    v.add(sl.toString());
 	}
 	return v.toArray(new String[0]);
     }
 
     String[] getAvailableStemmers() {
 	Vector<String> v = new Vector<String>();
 	v.add("GermanStemmer2Adapter");
 	v.add("PorterStemmer");
 	v.add("StaticStemmer");
 	return v.toArray(new String[0]);
     }
 
     /**
      * 
      * @see org.dotplot.tokenizer.filter.ui.IFilterUI#getController(org.dotplot.ui
      *      .ConfigurationView)
      */
     @Override
     public ViewController getController(ConfigurationView view) {
	this.view = view;
 	return new StemmerFilterUIController(view, this);
     }
 
     public StemmerLanguage getSelectedLanguage() {
 	if (selectLanguageCombo != null && !selectLanguageCombo.isDisposed()) {
 	    for (StemmerLanguage s : getAvailableLanguages()) {
		if (selectLanguageCombo.getText().trim().equals(s)) {
 		    return s;
 		}
 	    }
 	}
 	return null;
     }
 
     public String getSelectedStemmer() {
 	if (selectStemmerCombo != null && !selectStemmerCombo.isDisposed()) {
 	    for (String s : getAvailableStemmers()) {
 		if (selectStemmerCombo.getText().trim().equals(s)) {
 		    return s;
 		}
 	    }
 	}
 	return null;
     }
 
     public boolean isStemmerFilter() {
 	if (this.stemmerCheckBox != null && !this.stemmerCheckBox.isDisposed()) {
 	    return this.stemmerCheckBox.getSelection();
 	} else {
 	    return false;
 	}
     }
 
     /**
      * 
      * @see org.dotplot.tokenizer.filter.ui.AbstractFilterUI#refresh()
      */
     @Override
     public void refresh() {
     }
 
     /**
      * 
      * @see org.dotplot.tokenizer.filter.ui.IFilterUI#reset(org.dotplot.tokenizer
      *      .filter.IFilterConfiguration)
      */
     @Override
     public void reset(IFilterConfiguration config) {
 	if (config.getFilterList().contains(this.getFilterID())) {
 	    this.stemmerCheckBox.setSelection(true);
 	    this.selectStemmerCombo.setEnabled(true);
 
 	    Map<String, ? extends Object> parameter = config
 		    .getFilterParameter(this.getFilterID());
 	    Object value = parameter.get(SentenceFilter.PARAM_TOKEN_VALUE);
 	    if (value != null) {
 		String items[] = this.selectStemmerCombo.getItems();
 		for (int i = 0; i < items.length; i++) {
 		    if (value.equals(items[i].trim())) {
 			this.selectStemmerCombo.select(i);
 			return;
 		    }
 		}
 	    }
 	} else {
 	    this.stemmerCheckBox.setSelection(false);
 	    this.selectStemmerCombo.setEnabled(false);
 	}
 	this.selectStemmerCombo.clearSelection();
 	this.selectStemmerCombo.deselectAll();
     }
 
 }
