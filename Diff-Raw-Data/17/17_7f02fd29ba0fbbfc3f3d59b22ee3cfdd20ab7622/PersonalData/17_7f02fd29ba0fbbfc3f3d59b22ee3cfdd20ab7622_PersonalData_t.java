 package org.bouncycastle.asn1.x509.sigi;
 
 import org.bouncycastle.asn1.ASN1Encodable;
 import org.bouncycastle.asn1.ASN1EncodableVector;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1TaggedObject;
 import org.bouncycastle.asn1.DERGeneralizedTime;
 import org.bouncycastle.asn1.DERInteger;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.DERPrintableString;
 import org.bouncycastle.asn1.DERSequence;
 import org.bouncycastle.asn1.DERTaggedObject;
 import org.bouncycastle.asn1.x500.DirectoryString;
 
 import java.math.BigInteger;
 import java.util.Enumeration;
 
 /**
  * Contains personal data for the otherName field in the subjectAltNames
  * extension.
  * <p/>
  * <pre>
  *     PersonalData ::= SEQUENCE {
  *       nameOrPseudonym NameOrPseudonym,
  *       nameDistinguisher [0] INTEGER OPTIONAL,
  *       dateOfBirth [1] GeneralizedTime OPTIONAL,
  *       placeOfBirth [2] DirectoryString OPTIONAL,
  *       gender [3] PrintableString OPTIONAL,
  *       postalAddress [4] DirectoryString OPTIONAL
  *       }
  * </pre>
  *
  * @see org.bouncycastle.asn1.x509.sigi.NameOrPseudonym
  * @see org.bouncycastle.asn1.x509.sigi.SigIObjectIdentifiers
  */
 public class PersonalData
     extends ASN1Encodable
 {
 
     private NameOrPseudonym nameOrPseudonym = null;
 
     private BigInteger nameDistiguisher = null;
 
     private DERGeneralizedTime dateOfBirth = null;
 
     private DirectoryString placeOfBirth = null;
 
     private DERPrintableString gender = null;
 
     private DirectoryString postalAddress = null;
 
     public static PersonalData getInstance(Object obj)
     {
         if (obj == null || obj instanceof PersonalData)
         {
             return (PersonalData)obj;
         }
 
         if (obj instanceof ASN1Sequence)
         {
             return new PersonalData((ASN1Sequence)obj);
         }
 
         throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
     }
 
     /**
      * Constructor from ASN1Sequence.
      * <p/>
      * The sequence is of type NameOrPseudonym:
      * <p/>
      * <pre>
      *     PersonalData ::= SEQUENCE {
      *       nameOrPseudonym NameOrPseudonym,
      *       nameDistinguisher [0] INTEGER OPTIONAL,
      *       dateOfBirth [1] GeneralizedTime OPTIONAL,
      *       placeOfBirth [2] DirectoryString OPTIONAL,
      *       gender [3] PrintableString OPTIONAL,
      *       postalAddress [4] DirectoryString OPTIONAL
      *       }
      * </pre>
      *
      * @param seq The ASN.1 sequence.
      */
     private PersonalData(ASN1Sequence seq)
     {
         if (seq.size() < 1)
         {
             throw new IllegalArgumentException("Bad sequence size: "
                 + seq.size());
         }
 
         Enumeration e = seq.getObjects();
 
         nameOrPseudonym = NameOrPseudonym.getInstance(e.nextElement());
 
         while (e.hasMoreElements())
         {
             ASN1TaggedObject o = ASN1TaggedObject.getInstance(e.nextElement());
             int tag = o.getTagNo();
             switch (tag)
             {
                 case 0:
                    nameDistiguisher = DERInteger.getInstance(o, false).getValue();
                     break;
                 case 1:
                    dateOfBirth = DERGeneralizedTime.getInstance(o, false);
                     break;
                 case 2:
                     placeOfBirth = DirectoryString.getInstance(o, true);
                     break;
                 case 3:
                    gender = DERPrintableString.getInstance(o, false);
                     break;
                 case 4:
                     postalAddress = DirectoryString.getInstance(o, true);
                     break;
                 default:
                     throw new IllegalArgumentException("Bad tag number: " + o.getTagNo());
             }
         }
     }
 
     /**
      * Constructor from a given details.
      *
      * @param nameOrPseudonym  Name or pseudonym.
      * @param nameDistiguisher Name distinguisher.
      * @param dateOfBirth      Date of birth.
      * @param placeOfBirth     Place of birth.
      * @param gender           Gender.
      * @param postalAddress    Postal Address.
      */
     public PersonalData(NameOrPseudonym nameOrPseudonym,
                         BigInteger nameDistiguisher, DERGeneralizedTime dateOfBirth,
                         String placeOfBirth, String gender, String postalAddress)
     {
         this.nameOrPseudonym = nameOrPseudonym;
         this.dateOfBirth = dateOfBirth;
         this.gender = new DERPrintableString(gender, true);
         this.nameDistiguisher = nameDistiguisher;
         this.postalAddress = new DirectoryString(postalAddress);
         this.placeOfBirth = new DirectoryString(placeOfBirth);
     }
 
     /**
      * Produce an object suitable for an ASN1OutputStream.
      * <p/>
      * Returns:
      * <p/>
      * <pre>
      *     PersonalData ::= SEQUENCE {
      *       nameOrPseudonym NameOrPseudonym,
      *       nameDistinguisher [0] INTEGER OPTIONAL,
      *       dateOfBirth [1] GeneralizedTime OPTIONAL,
      *       placeOfBirth [2] DirectoryString OPTIONAL,
      *       gender [3] PrintableString OPTIONAL,
      *       postalAddress [4] DirectoryString OPTIONAL
      *       }
      * </pre>
      *
      * @return a DERObject
      */
     public DERObject toASN1Object()
     {
         ASN1EncodableVector vec = new ASN1EncodableVector();
         vec.add(nameOrPseudonym);
         if (nameDistiguisher != null)
         {
            vec.add(new DERTaggedObject(false, 0, new DERInteger(nameDistiguisher)));
         }
         if (dateOfBirth != null)
         {
            vec.add(new DERTaggedObject(false, 1, dateOfBirth));
         }
         if (placeOfBirth != null)
         {
             vec.add(new DERTaggedObject(true, 2, placeOfBirth));
         }
         if (gender != null)
         {
            vec.add(new DERTaggedObject(false, 3, gender));
         }
         if (postalAddress != null)
         {
             vec.add(new DERTaggedObject(true, 4, postalAddress));
         }
         return new DERSequence(vec);
 	}
 }
