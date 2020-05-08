 /*
  * File name: Formula.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2012 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package diabelli.logic;
 
 import diabelli.Diabelli;
 import diabelli.FormulaFormatManager;
 import diabelli.components.GoalProvidingReasoner;
 import diabelli.logic.FormulaTranslator.TranslationException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.netbeans.api.annotations.common.NonNull;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 
 /**
  * Represents a general formula. It can be a diagrammatic, sentential, or both
  * at the same time (if the {@link GoalProvidingReasoner reasoner} provides more
  * than one representation of this formula). This class can thus carry many
  * representations, or formats, of the same formula. For example, a formula can
  * be represented with many strings (using syntaxes of many theorem provers),
  * with term trees (abstract syntax trees), or similar. However, there is always
  * {@link Formula#getMainRepresentation() one main representation}. Other
  * representations should be equivalent to it, or at least have the proper
  * logical relation to it (i.e., if this formula is {@link Goal#getPremises() a
  * premise}, then all other representations of it must be logically entailed by
  * the {@link Formula#getMainRepresentation() main representation}; on the other
  * hand, when the formula is {@link Goal#getConclusion() a conclusion}, then the
  * other direction of entailment must hold; the representations can be logically
  * equivalent, i.e., mutually entailed, which is always allowed).
  *
  * <p><span style="font-weight:bold">Note</span>: a formula may have only one
  * representation in a particular {@link FormulaFormatDescriptor format}.</p>
  *
  * @param <T> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
  * of {@link Formula#getMainRepresentation() the main representation} of this formula.
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 @NbBundle.Messages({
     "Formula_null_main_representation=The formula must have a main representation."
 })
 public class Formula<T> {
 
     // <editor-fold defaultstate="collapsed" desc="Fields">
     private final FormulaRepresentation<T> mainRepresentation;
     /**
      * I have decided to use a multimap of representations instead of a simple
      * map. The thing is that one there can be many representations for a single
      * format.
      *
      * <p>Detailed specification:
      *
      * <ul>
      *
      * <li>if this hash map returns {@code null} for a given format, then this
      * means that no attempt on finding a representation for this formula in the
      * given format has been made. Therefore, it is sensible to try and obtain
      * translations in this case,</li>
      *
      * <li>if this hash map returns a non-{@code null} value, then this
      * indicates that a translation attempt through {@link Formula#fetchRepresentations(diabelli.logic.FormulaFormat)}
      * has been made but no translation has been found. In this case, there is
      * no need to search for a translation again.</li>
      *
      * </ul>
      *
      * </p>
      */
     private final HashMap<String, HashSet<FormulaRepresentation<?>>> representations;
     private final FormulaRole role;
     // </editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Constructors">
     /**
      * Creates a formula with the given list of different representations.
      *
      * @param mainRepresentation the main representation of this formula.
      * <p>Other representations must be either entailed by this representation
      * (if this formula acts as a premise) or they must entail the main
      * representation (if this formula acts as a conclusion).</p>
      * @param otherRepresentations this list of representations must contain at
      * least one element. The first element of the list will become the {@link
      * Formula#getMainRepresentation() main representation}.
      * @param role the role of this formula in a {@link Goal}.
      */
     @NbBundle.Messages({
         "F_role_null=A role must be provided for this formula."
     })
     public Formula(@NonNull FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, Collection<FormulaRepresentation<?>> otherRepresentations) {
         if (mainRepresentation == null) {
             throw new IllegalArgumentException(Bundle.Formula_null_main_representation());
         }
         if (role == null) {
             throw new IllegalArgumentException(Bundle.F_role_null());
         }
         // Initialise the fields:
         this.representations = new HashMap<>();
         this.mainRepresentation = mainRepresentation;
         // Add the main representation to the registry too:
         putRepresentation(mainRepresentation.getFormat(), mainRepresentation);
         // Now add the other representations:
         if (otherRepresentations != null && !otherRepresentations.isEmpty()) {
             for (FormulaRepresentation<?> otherRepresentation : otherRepresentations) {
                 putRepresentation(otherRepresentation);
             }
         }
         this.role = role;
     }
 
     /**
      * Creates a formula with the given list of different representations.
      *
      * @param mainRepresentation the main representation of this formula.
      * <p>Other representations must be either entailed by this representation
      * (if this formula acts as a premise) or they must entail the main
      * representation (if this formula acts as a conclusion).</p>
      * @param otherRepresentations this list of representations must contain at
      * least one element. The first element of the list will become the {@link
      * Formula#getMainRepresentation() main representation}.
      * @param role the role of this formula in a {@link Goal}.
      */
     public Formula(FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, FormulaRepresentation<?>... otherRepresentations) {
         this(mainRepresentation, role, otherRepresentations == null || otherRepresentations.length < 1 ? null : Arrays.asList(otherRepresentations));
     }
 
     /**
      * Creates a formula with the given list of different representations.
      *
      * @param mainRepresentation the main representation of this formula.
      * <p>Other representations must be either entailed by this representation
      * (if this formula acts as a premise) or they must entail the main
      * representation (if this formula acts as a conclusion).</p>
      * @param otherRepresentations this list of representations must contain at
      * least one element. The first element of the list will become the {@link
      * Formula#getMainRepresentation() main representation}.
      * @param role the role of this formula in a {@link Goal}.
      */
     public Formula(FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, ArrayList<FormulaRepresentation<?>> otherRepresentations) {
         this(mainRepresentation, role, (Collection<FormulaRepresentation<?>>) otherRepresentations);
     }
     //</editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Public Properties">
     /**
      * Returns the main representation of this formula. This is usually the
      * native formula representation of the {@link GoalProvidingReasoner
      * reasoner} that provided this formula.
      *
      * <p>Other representations must be either entailed by this representation
      * (if this formula acts as a premise) or they must entail the main
      * representation (if this formula acts as a conclusion).</p>
      *
      * @return the main representation of this formula.
      */
     public FormulaRepresentation<T> getMainRepresentation() {
         return mainRepresentation;
     }
 
     /**
      * Returns the formats of all currently present/calculated representations
      * of this formula. This collection includes the main representation.
      *
      * <p><span style="font-weight:bold">Note</span>: this method returns only
      * those formats for which there is an actual representation of this formula
      * present.</p>
      * 
      * <p>This method always returns a non-{@code null} value.</p>
      *
      * @return all formats into which we translated the formula.
      */
     @NonNull
     public ArrayList<FormulaFormat<?>> getFormats() {
         synchronized (representations) {
             ArrayList<FormulaFormat<?>> formats = new ArrayList<>();
             FormulaFormatManager formatManager = Lookup.getDefault().lookup(Diabelli.class).getFormulaFormatManager();
             for (Map.Entry<String, HashSet<FormulaRepresentation<?>>> formatEntry : representations.entrySet()) {
                 if (formatEntry.getValue() != null && formatEntry.getValue().size() > 0) {
                     formats.add(formatManager.getFormulaFormat(formatEntry.getKey()));
                 }
             }
             return formats;
         }
     }
 
     /**
      * Returns the number of formats this formula has representations in. The
      * minimum this function can return is {@code 1}.
      *
      * <p><span style="font-weight:bold">Note</span>: it might be that there is
      * no representation of this formula in a format, even though the format is
      * listed in {@link Formula#getFormats()}. This means that an attempt on
      * translating the formula to the format has been made via {@link Formula#fetchRepresentations(diabelli.logic.FormulaFormatDescriptor)}
      * but it yielded no results──the translation did not succeed.</p>
      *
      * @return the number of representations this formula has.
      */
     public int getFormatsCount() {
         synchronized (representations) {
             return representations.size();
         }
     }
 
     /**
      * Returns the role of this formula in a {@link Goal}.
      *
      * @return the role of this formula in a {@link Goal}.
      */
     public FormulaRole getRole() {
         return role;
     }
 
     /**
      * Returns the representations of this formula in the given format. This
      * method does not try to convert the formula into the given format. It only
      * gives already present representations. To try and automatically calculate
      * a representation of this formula in the given format, use {@link Formula#fetchRepresentations(diabelli.logic.FormulaFormat)
      * }.
      *
      * <p>This function returns {@code null} if there is no translation of the
      * formula to the given format.</p>
      *
      * @param <TRepresentation> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
      * carried by the returned representations.
      * @param format the desired format in which to get this formula.
      * @return the translations of the {@link Formula#getMainRepresentation()
      * formula} in the given format.
      */
     @SuppressWarnings("unchecked")
     public <TRepresentation> FormulaRepresentation<TRepresentation>[] getRepresentations(FormulaFormat<TRepresentation> format) {
         if (format == null) {
             throw new IllegalArgumentException(Bundle.F_toFormat_null());
         }
         synchronized (representations) {
             HashSet<FormulaRepresentation<?>> formatReps = representations.get(format.getFormatName());
             return (FormulaRepresentation<TRepresentation>[]) (formatReps == null || formatReps.isEmpty() ? null : formatReps.toArray(new FormulaRepresentation<?>[formatReps.size()]));
         }
     }
     
     /**
      * Returns a representation of this formula in the given format.
      * 
      * <p>There may be more than one representation of this formula in the given
      * format. If so, an arbitrary one is returned.</p>
      * 
      * @param <TRep> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
      * carried by the returned representation.
      * @param format the desired format in which to get this formula.
      * @return the translation of the {@link Formula#getMainRepresentation()
      * formula} in the given format.
      */
     public <TRep> FormulaRepresentation<TRep> getRepresentation(FormulaFormat<TRep> format) {
         if (format == null) {
             throw new IllegalArgumentException(Bundle.F_toFormat_null());
         }
         synchronized (representations) {
             HashSet<FormulaRepresentation<?>> formatReps = representations.get(format.getFormatName());
             return (FormulaRepresentation<TRep>) (formatReps == null || formatReps.isEmpty() ? null : formatReps.iterator().next());
         }
     }
 
     /**
      * Returns the number of representations of this formula in the given
      * format.
      *
      * @param format the format for which we want to get the number of
      * representations.
      * @return the number of representations of this formula in the given
      * format.
      */
     public int getRepresentationsCount(FormulaFormat<?> format) {
         if (format == null) {
             throw new IllegalArgumentException(Bundle.F_toFormat_null());
         }
         synchronized (representations) {
             HashSet<FormulaRepresentation<?>> formatReps = representations.get(format.getFormatName());
             return formatReps == null || formatReps.isEmpty() ? 0 : formatReps.size();
         }
     }
 
     /**
      * Tries to convert this formula into the given format and returns the
      * translated representation if the translation succeeded.
      *
      * <p>This function returns {@code null} if there is no translation of the
      * formula to the given format.</p>
      *
      * <p><span style="font-weight:bold">Note</span>: if a translation of this
      * format doesn't exist yet, this method will try and translate it with the
      * help of the {@link FormulaFormatManager#getFormulaTranslators() registered translators}
      * in the {@link Diabelli#getFormulaFormatManager() formula format manager}.</p>
      *
      * <p>This method is thread-safe.</p>
      *
      * <p>This method is quite expensive.</p>
      *
      * @param <TRepresentation> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
      * carried by the returned representations.
      * @param format the desired format in which to get this formula.
      * @return the translation of the {@link Formula#getMainRepresentation()
      * formula}.
      */
     @NbBundle.Messages({
         "F_toFormat_null=A target format has to be specified."
     })
     @SuppressWarnings("unchecked")
     public <TRepresentation> FormulaRepresentation<TRepresentation>[] fetchRepresentations(FormulaFormat<TRepresentation> format) {
         if (format == null) {
             throw new IllegalArgumentException(Bundle.F_toFormat_null());
         }
         // If the representations in this format have already been calculated
         // once, return what is already available (it does not matter if no
         // translations are available).
         synchronized (representations) {
             if (representations.containsKey(format.getFormatName())) {
                 return getRepresentations(format);
             }
         }
         // Try to translate this formula:
         FormulaRepresentation<TRepresentation> representation = null;
         // There is no representation yet for this format. Try to find one.
         for (FormulaTranslator<?, ?> translator : Lookup.getDefault().lookup(Diabelli.class).getFormulaFormatManager().getFormulaTranslators()) {
             // Make sure that the translation is valid:
             if (translator.getFromFormat().equals(getMainRepresentation().getFormat())
                     && translator.getToFormat().equals(format)
                     && getRole().isTranslationApplicable(translator.getTranslationType())) {
                 try {
                     // We can try and translate it:
                     Formula<TRepresentation> otherRep = ((FormulaTranslator<T, TRepresentation>) translator).translate(this);
                     if (otherRep != null) {
                         // We got a translation, add it to the collection of
                         // all representations of this formula and return it
                         representation = otherRep.getMainRepresentation();
                         break;
                     }
                 } catch (TranslationException ex) {
                     Logger.getLogger(Formula.class.getName()).log(Level.FINEST, String.format("Translation with '%s' failed. Translation error message: %s", translator.getPrettyName(), ex.getMessage()), ex);
                 }
             }
         }
         // Put the found representation into the collection of all representatios.
         // In case the translation didn't succeed, null will indicate that in the
         // future no automatic translation attempts need to be made.
         putRepresentation(format, representation);
         return (FormulaRepresentation<TRepresentation>[])(representation == null ? null : new FormulaRepresentation<?>[]{representation});
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Helper Classes">
     /**
      * Indicates the role of the {@link Formula formula} in a goal.
      */
     public static enum FormulaRole {
 
         /**
          * Indicates that the {@link Formula formula} is {@link
          * Goal#getPremises() a premise}.
          */
         Premise,
         /**
          * Indicates that the {@link Formula formula} is {@link
          * Goal#getConclusion() the conclusion}.
          */
         Conclusion,
         /**
          * Indicates that the {@link Formula formula} is {@link
          * Goal#asFormula() the goal itself}.
          */
         Goal;
 
         /**
          * Checks whether the translation of the given type is applicable on a
          * formula of this role.
          *
          * @param transType the type of the translation.
          * @return a value indicating whether the translation of the given type
          * is applicable on a formula of this role.
          */
         public boolean isTranslationApplicable(FormulaTranslator.TranslationType transType) {
             return this == Premise ? transType == FormulaTranslator.TranslationType.ToEquivalent || transType == FormulaTranslator.TranslationType.ToEntailed
                     : this == Conclusion ? transType == FormulaTranslator.TranslationType.ToEquivalent || transType == FormulaTranslator.TranslationType.ToEntailing
                     : transType == FormulaTranslator.TranslationType.ToEquivalent;
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Private Helper Methods">
     @NbBundle.Messages({
         "F_representation_null=Only valid non-null representations can be added to a formula."
     })
     private <T> void putRepresentation(FormulaRepresentation<T> representation) {
         if (representation == null) {
             throw new IllegalArgumentException(Bundle.F_representation_null());
         }
         putRepresentation(representation.getFormat(), representation);
     }
 
     @NbBundle.Messages({
         "F_format_null=The representation to be added does not identify its format. A valid format must be provided."
     })
     private <T> void putRepresentation(FormulaFormat<T> format, FormulaRepresentation<T> representation) {
         if (format == null) {
             throw new IllegalArgumentException(Bundle.F_format_null());
         }
         synchronized (representations) {
             HashSet<FormulaRepresentation<?>> formatReps = representations.get(format.getFormatName());
             if (formatReps == null) {
                 formatReps = new HashSet<>();
                 representations.put(format.getFormatName(), formatReps);
             }
             if (representation != null) {
                 formatReps.add(representation);
             }
         }
     }
     // </editor-fold>
 }
