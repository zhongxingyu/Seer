 package de.htw.berlin.student.polynom.controller;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import de.htw.berlin.student.polynom.model.DividedPolynom;
 import de.htw.berlin.student.polynom.model.Polynom;
 
 /**
  * Class that does the calculation for a given polynom.
  * 
  * @author Matthias Drummer
  */
 public class PolynomCalculator {
 
 	/**
 	 * Evaluates the polynom via the horner schema.
 	 * 
 	 * @param polynom the {@link Polynom} to evaluate
 	 * @param x the value for x used for the evaluation
 	 * @see http://rosettacode.org/wiki/Horner's_rule_for_polynomial_evaluation#Java
 	 * 
 	 * @return the value for the evaluated polynom
 	 */
 	public BigDecimal evaluate(Polynom polynom, BigDecimal x) {
 
 		BigDecimal result = null;
 		List<BigDecimal> coefficients = new ArrayList<BigDecimal>();
 		coefficients.addAll(polynom.getCoefficients());
 
 		Collections.reverse(coefficients);
 
 		result = coefficients.get(0);
 		// start with the x^n coefficient
 		for (int i = 1; i < coefficients.size(); i++) {
 			result = (result.multiply(x)).add(coefficients.get(i));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Multiplies two given polynoms.
 	 * 
 	 * @see http://sol.cs.hm.edu/dpunkt-java-praktikum/polynomial/Polynomial.java
 	 * 
 	 * @param poly1 the first polynom
 	 * @param poly2 the second polynom
 	 * @return the result
 	 */
 	public Polynom multiply(Polynom poly1, Polynom poly2) {
 
 		if (poly1 == null || poly2 == null) {
 			throw new IllegalArgumentException("None of the given polynoms may be null.");
 		}
 
 		// check for special cases
 		if (poly1.getCoefficients().isEmpty() || poly2.getCoefficients().isEmpty()) {
 			return new Polynom(Collections.<BigDecimal> emptyList());
 		}
 		// if a polynom has one element which value is one then return the other polynom.
 		if (poly1.getCoefficients().size() == 1 && poly1.getCoefficients().get(0).equals(new BigDecimal(1))) {
 			return poly2;
 		}
 		if (poly2.getCoefficients().size() == 1 && poly2.getCoefficients().get(0).equals(new BigDecimal(1))) {
 			return poly1;
 		}
 
 		// do calculation
 		BigDecimal[] coeff1 = (BigDecimal[]) poly1.getCoefficients().toArray();
 		BigDecimal[] coeff2 = (BigDecimal[]) poly2.getCoefficients().toArray();
 
 		BigDecimal[] multiplied = new BigDecimal[Math.max(coeff1.length, coeff2.length) + 1];
 		for (int i = 0; i < coeff1.length; i++) {
 			for (int j = 0; j < coeff2.length; j++) {
 				int newIndex = i + j;
 				if (multiplied[newIndex] == null) {
 					multiplied[newIndex] = new BigDecimal(0);
 				}
 				multiplied[newIndex].add(coeff1[i].multiply(coeff2[j]));
 			}
 		}
 
 		return new Polynom(Arrays.asList(multiplied));
 	}
 
 	/**
 	 * Subtracts two poylnoms.
 	 * 
 	 * @param poly1 the first polynom
 	 * @param poly2 the second polynom to substract from the first one
 	 * @return the result
 	 */
 	public Polynom subtract(Polynom poly1, Polynom poly2) {
 
 		Polynom result = null;
 
 		List<BigDecimal> substractedCoeff = new ArrayList<BigDecimal>();
 
 		// determine the number of iterations to do
 		int maxLength = poly1.getCoefficients().size() >= poly2.getCoefficients().size() ? poly1.getCoefficients().size() : poly2.getCoefficients().size();
 
 		for (int i = 0; i < maxLength; i++) {
 
 			BigDecimal coef1 = new BigDecimal(0); // is the coeff of listOne for the index if list has one else zero BigDecimal.
 			BigDecimal coef2 = new BigDecimal(0);// is the coeff of listOne for the index if list has one else zero BigDecimal.
 
 			if (i < poly1.getCoefficients().size()) {
 				coef1 = poly1.getCoefficients().get(i);
 			}
 			if (i < poly2.getCoefficients().size()) {
 				coef2 = poly2.getCoefficients().get(i);
 			}
 			substractedCoeff.add(coef1.subtract(coef2));
 		}
 		result = new Polynom(substractedCoeff);
 
 		return result;
 	}
 
 	/**
 	 * Adds two given polynoms.
 	 * 
 	 * @param poly1 the first polynom
 	 * @param poly2 the second polynom to be added to the first one
 	 * @return the result
 	 */
 	public Polynom add(Polynom poly1, Polynom poly2) {
 
 		Polynom result = null;
 
 		List<BigDecimal> addedCoeff = new ArrayList<BigDecimal>();
 
 		// determine which polynom has more coefficients
 		List<BigDecimal> listOne;
 		List<BigDecimal> listTwo;
 		if (poly1.getCoefficients().size() >= poly2.getCoefficients().size()) {
 			listOne = poly1.getCoefficients();
 			listTwo = poly2.getCoefficients();
 		} else {
 			listOne = poly2.getCoefficients();
 			listTwo = poly1.getCoefficients();
 		}
 
 		for (int i = 0; i < listOne.size(); i++) {
 			BigDecimal coef1 = listOne.get(i);
 			BigDecimal coef2 = new BigDecimal(0);
 			if (i < listTwo.size()) {
 				coef2 = listTwo.get(i);
 			}
 			addedCoeff.add(coef1.add(coef2));
 		}
 		result = new Polynom(addedCoeff);
 
 		return result;
 	}
 
 	/**
	 * Divides a polynom by (x - given value).
 	 * 
 	 * @param poly1 the first polynom
 	 * @param value the value to divide by (x-value)
 	 * @return the result of division
 	 */
 	public DividedPolynom divideWithHorner(Polynom poly1, BigDecimal value) {
 
 		List<BigDecimal> newCoeffs = new ArrayList<BigDecimal>();
 
 		List<BigDecimal> poly1Coeffs = poly1.getCoefficients();
 		for (int i = poly1Coeffs.size() - 1; i >= 0; i--) {
 			if (i == poly1Coeffs.size() - 1) {
 				newCoeffs.add(poly1.getCoefficients().get(i));
 			} else {
 				newCoeffs.add(value.multiply(newCoeffs.get(i - 1)).add(poly1Coeffs.get(i)));
 			}
 		}
 
 		List<BigDecimal> coefficients = new ArrayList<BigDecimal>();
 		// start with size - 2 because we dont want to have the rest of the division in our coefficients list
 		for (int i = newCoeffs.size() - 2; i >= 0; i--) {
 			coefficients.add(newCoeffs.get(i));
 		}
 
 		Polynom polynom = new Polynom(coefficients);
 		BigDecimal rest = newCoeffs.get(newCoeffs.size() - 1);
 
 		return new DividedPolynom(polynom, rest);
 	}
 
 	/**
 	 * Do the first derivate of a polynom.
 	 * 
 	 * @param polynom the polynom to be derivated
 	 * @return the new polynom which represents the first derivation of a polynom.
 	 */
 	public Polynom derivate(Polynom polynom) {
 
 		Polynom result = null;
 		List<BigDecimal> newCoeffs = new ArrayList<BigDecimal>();
 
 		for (int i = 1; i < polynom.getCoefficients().size(); i++) {
 			// ignore x^0 (i=0). It will perish.
 			newCoeffs.add(polynom.getCoefficients().get(i).multiply(new BigDecimal(i)));
 		}
 
 		result = new Polynom(newCoeffs);
 
 		return result;
 	}
 }
