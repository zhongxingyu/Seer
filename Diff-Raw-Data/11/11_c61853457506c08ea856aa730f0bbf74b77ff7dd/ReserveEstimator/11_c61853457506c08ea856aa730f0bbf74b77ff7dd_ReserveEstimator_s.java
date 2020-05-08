 package models.paramest;
 
 
 import edu.umich.eecs.tac.props.BidBundle;
 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 import edu.umich.eecs.tac.props.QueryType;
 
 import java.util.Set;
 
 import static models.paramest.ConstantsAndFunctions.*;
 
 public class ReserveEstimator {
 
    QueryType _queryType;
    Set<Query> _querySpace;
    int _qTypeIdx;
    double _currentRegEstimate;
    double _minRegBidWithImps;
    double _currentPromEstimate;
    double _minPromBidWithImps;
    int _numPromSlots;
    double _squashParam;
 
    public ReserveEstimator(int numPromSlots, double squashParam, QueryType qt, Set<Query> querySpace) {
       _numPromSlots = numPromSlots;
       _squashParam = squashParam;
       _queryType = qt;
       _querySpace = querySpace;
 
       _qTypeIdx = queryTypeToInt(_queryType);
       _currentRegEstimate = _regReserveLow[_qTypeIdx];
       _minRegBidWithImps = _regReserveHigh[_qTypeIdx];
 
       _currentPromEstimate = _regReserveLow[_qTypeIdx];
       _minPromBidWithImps = _regReserveHigh[_qTypeIdx] + 0.5;
    }
 
    /*
     * TODO: This should take in adv effects
     */
    public boolean updateModel(BidBundle bundle, QueryReport queryReport) {
 
       for(Query q : _querySpace) {
          if(q.getType().equals(_queryType)) {
             double bid = bundle.getBid(q);
             double squashedBid = Math.pow(_advertiserEffectBoundsAvg[_qTypeIdx],_squashParam) * bid;
             /*
             * Check if there are any times that our bid was above what we believe
             * the reserve to be, and we weren't in the auction when we could have been
             */
             if(queryReport.getImpressions(q) == 0 && squashedBid > _currentRegEstimate) {
                boolean avgPos5 = false;
                for(int i = 0; i < 8; i++) {
                   double avgPos = queryReport.getPosition(q,"adv" + (i+1));
                   if(!Double.isNaN(avgPos) && avgPos == 5.0) {
                      avgPos5 = true;
                      break;
                   }
                }
 
                if(!avgPos5) {
                   //This means that there was no one in slot 5 the whole time
                   //and we weren't in the auction, so our bid is below the reserve
                   _currentRegEstimate = squashedBid;
                }
             }
 
 
             /*
             * Check if there were any times that we were in a promoted slot, but
             * got no promoted impressions
             */
             if(queryReport.getImpressions(q) > 0 &&
                     queryReport.getPromotedImpressions(q) == 0 &&
                     !Double.isNaN(queryReport.getPosition(q)) &&
                     queryReport.getPosition(q) <= _numPromSlots &&
                     squashedBid > _currentPromEstimate) {
                _currentPromEstimate = squashedBid;
             }
          }
       }
 
       /*
       * Find the minimum bid that we received imps with
       *
       */
       for(Query q : _querySpace) {
          if(q.getType().equals(_queryType)) {
             double bid = bundle.getBid(q);
             double squashedBid = Math.pow(_advertiserEffectBoundsAvg[_qTypeIdx],_squashParam) * bid;
             if(queryReport.getImpressions(q) > 0 && squashedBid < _minRegBidWithImps) {
                _minRegBidWithImps = squashedBid;
             }
          }
       }
 
       /*
       * Find the minimum bid that we received prom imps with
       */
       for(Query q : _querySpace) {
          if(q.getType().equals(_queryType)) {
             double bid  = bundle.getBid(q);
             double squashedBid = Math.pow(_advertiserEffectBoundsAvg[_qTypeIdx],_squashParam) * bid;
             if(queryReport.getPromotedImpressions(q) > 0 && squashedBid < _minPromBidWithImps) {
                _minPromBidWithImps = squashedBid;
             }
          }
       }
 
       _currentRegEstimate = Math.min(_currentRegEstimate, _minRegBidWithImps);
       _currentRegEstimate = Math.min(_regReserveHigh[_qTypeIdx],_currentRegEstimate);
       _currentRegEstimate = Math.max(_regReserveLow[_qTypeIdx],_currentRegEstimate);
 
       _currentPromEstimate = Math.min(_currentPromEstimate,_minPromBidWithImps);
       _currentPromEstimate = Math.min(_currentRegEstimate + 0.5,_currentPromEstimate);
       _currentPromEstimate = Math.max(_currentRegEstimate,_currentPromEstimate);
 
       return true;
    }
 
    public double getRegPrediction() {
       return ((_currentRegEstimate+_minRegBidWithImps)/2.0);
    }
 
    public double getPromPrediction() {
       return ((_currentPromEstimate+_minPromBidWithImps)/2.0);
    }
 }
